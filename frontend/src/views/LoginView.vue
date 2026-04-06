<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { loginWithEmailPassword, registerAccount } from '../api/account'
import { saveSession } from '../session/storage'

const router = useRouter()

const loginBgUrl = new URL('../assets/images/login_background.png', import.meta.url).href
const logoUrl = new URL('../assets/images/beautiful_big_logo.png', import.meta.url).href

const mode = ref('login')
const error = ref('')
const loading = ref(false)

const email = ref('')
const password = ref('')
const username = ref('')
const address = ref('')
const accountType = ref('CUSTOMER')

/**
 * submit login form
 * redirect to app shell if successful, set error message if not
 * @returns {Promise<void>}
 *
 * @author Zirun Wang
 */
async function submitLogin() {
  error.value = ''
  loading.value = true
  try {
    const account = await loginWithEmailPassword(email.value.trim(), password.value)
    saveSession(account)
    await router.replace({ name: 'app' })
  } catch (e) {
    error.value = e.message ?? 'Login failed'
  } finally {
    loading.value = false
  }
}

/**
 * submit register form
 * redirect to app shell if successful, set error message if not
 * @returns {Promise<void>}
 *
 * @author Zirun Wang
 */
async function submitRegister() {
  error.value = ''
  loading.value = true
  try {
    const account = await registerAccount({
      username: username.value.trim(),
      email: email.value.trim(),
      password: password.value,
      address: address.value.trim(),
      accountType: accountType.value,
    })
    saveSession(account)
    await router.replace({ name: 'app' })
  } catch (e) {
    error.value = e.message ?? 'Could not create account'
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div
    class="auth-page"
    :style="{ backgroundImage: `url(${loginBgUrl})` }"
  >
    <div class="auth-layout">
      <img
        class="brand-logo"
        :src="logoUrl"
        alt="Papa's Fashionista Ria"
        width="520"
        height="auto"
      />

      <div class="panel">
        <p class="tagline">Online fashion boutique</p>

        <div class="tabs" role="tablist">
          <button
            type="button"
            role="tab"
            :aria-selected="mode === 'login'"
            :class="{ active: mode === 'login' }"
            @click="mode = 'login'"
          >
            Log in
          </button>
          <button
            type="button"
            role="tab"
            :aria-selected="mode === 'register'"
            :class="{ active: mode === 'register' }"
            @click="mode = 'register'"
          >
            Create account
          </button>
        </div>

        <form v-if="mode === 'login'" class="form form--login" @submit.prevent="submitLogin">
          <label class="field">
            <span>Email</span>
            <input v-model="email" type="email" required autocomplete="username" />
          </label>
          <label class="field">
            <span>Password</span>
            <input v-model="password" type="password" required autocomplete="current-password" />
          </label>
          <p v-if="error" class="error" role="alert">{{ error }}</p>
          <button type="submit" class="primary" :disabled="loading">
            {{ loading ? 'Signing in…' : 'Sign in' }}
          </button>
        </form>

        <form v-else class="form form--register" @submit.prevent="submitRegister">
          <label class="field">
            <span>Account type</span>
            <select v-model="accountType">
              <option value="CUSTOMER">Customer</option>
              <option value="EMPLOYEE">Employee</option>
              <option value="MANAGER">Manager</option>
            </select>
          </label>
          <label class="field">
            <span>Username</span>
            <input v-model="username" required autocomplete="nickname" />
          </label>
          <label class="field">
            <span>Email</span>
            <input v-model="email" type="email" required autocomplete="email" />
          </label>
          <label class="field">
            <span>Password</span>
            <input
              v-model="password"
              type="password"
              required
              minlength="8"
              autocomplete="new-password"
            />
          </label>
          <label class="field field--full">
            <span>Address</span>
            <input v-model="address" required autocomplete="street-address" />
          </label>
          <p v-if="error" class="error field--full" role="alert">{{ error }}</p>
          <button type="submit" class="primary field--full" :disabled="loading">
            {{ loading ? 'Creating…' : 'Create account' }}
          </button>
        </form>
      </div>
    </div>
  </div>
</template>

<style scoped>
/*
  Logo width: change --login-logo-max-width (e.g. 400px, 32rem, min(600px, 90vw))
  to resize the header logo without editing markup.
*/
.auth-page {
  --login-logo-max-width: min(520px, 88vw);
  --auth-accent: #ffe5f9;
  --auth-accent-strong: #d8bff1;
  --auth-text: #3d3c3c;
  --auth-surface: rgba(255, 255, 255, 0.93);

  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: clamp(1rem, 4vw, 2.5rem);
  background-size: cover;
  background-position: center;
  background-repeat: no-repeat;
  background-color: #b8d4e8;
}

.auth-layout {
  width: 100%;
  max-width: 640px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: clamp(1rem, 3vw, 1.75rem);
}

.brand-logo {
  width: var(--login-logo-max-width);
  max-width: 100%;
  height: auto;
  display: block;
  object-fit: contain;
  filter: drop-shadow(0 4px 20px rgba(0, 0, 0, 0.12));
}

.panel {
  width: 100%;
  background: var(--auth-surface);
  border-radius: 16px;
  padding: clamp(1.25rem, 3.5vw, 2rem) clamp(1.25rem, 4vw, 2.25rem);
  box-shadow:
    0 4px 12px rgba(0, 0, 0, 0.25),
    0 0 0 1px rgba(255, 255, 255, 0.6) inset;
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
}

.tagline {
  margin: 0 0 1.25rem;
  font-size: 1rem;
  color: var(--auth-text);
  text-align: center;
  opacity: 0.9;
}

.tabs {
  display: flex;
  gap: 0;
  margin-bottom: 1.25rem;
  border-radius: 12px;
  overflow: hidden;
  border: 1.75px solid #3d3c3c;
}

.tabs button {
  flex: 1;
  padding: 0.7rem 1rem;
  border: none;
  background: #f7f7f7;
  color: var(--auth-text);
  font-size: 1rem;
  font-family: Inconsolata, monospace;
  cursor: pointer;
  transition: background 0.2s, color 0.2s;
}

.tabs button:hover {
  background: #eee;
}

.tabs button.active {
  background: linear-gradient(to right, var(--auth-accent) 49%, #ffffff 100%);
  color: var(--auth-text);
  font-weight: 600;
}

.form {
  font-family: Inconsolata, monospace;
}

.form--login {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.form--register {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 1rem;
  align-items: start;
}

.field {
  display: flex;
  flex-direction: column;
  gap: 0.35rem;
  font-size: 0.95rem;
  color: var(--auth-text);
  min-width: 0;
}

.field--full {
  grid-column: 1 / -1;
}

.field span {
  font-weight: 600;
}

.field input,
.field select {
  padding: 0.65rem 0.85rem;
  border: 1.75px solid #3d3c3c;
  border-radius: 8px;
  font-size: 1rem;
  font-family: Inconsolata, monospace;
  background: #fff;
  color: var(--auth-text);
  width: 100%;
}

.field input:focus,
.field select:focus {
  outline: 2px solid #1209ab;
  outline-offset: 1px;
  border-color: var(--auth-accent-strong);
}

.primary {
  margin-top: 0.25rem;
  padding: 0.8rem 1.25rem;
  border: none;
  border-radius: 12px;
  background: #3d3c3c;
  color: #fff;
  font-size: 1rem;
  font-family: Inconsolata, monospace;
  font-weight: 600;
  cursor: pointer;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.2);
  transition: transform 0.15s, box-shadow 0.15s, opacity 0.15s;
}

.primary:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: 0 4px 14px rgba(0, 0, 0, 0.22);
}

.primary:disabled {
  opacity: 0.65;
  cursor: not-allowed;
}

.error {
  margin: 0;
  font-size: 0.9rem;
  color: #a42020;
}

@media (max-width: 520px) {
  .form--register {
    grid-template-columns: 1fr;
  }

  .field--full {
    grid-column: auto;
  }
}
</style>
