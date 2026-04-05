<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { loginWithEmailPassword, registerAccount } from '../api/client'
import { saveSession } from '../session/storage'

const router = useRouter()

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
  <div class="auth-page">
    <div class="panel">
      <h1 class="brand">Papa's Fashionistaria</h1>
      <p class="tagline">Online fashion boutique</p>

      <div class="tabs">
        <button type="button" :class="{ active: mode === 'login' }" @click="mode = 'login'">
          Log in
        </button>
        <button type="button" :class="{ active: mode === 'register' }" @click="mode = 'register'">
          Create account
        </button>
      </div>

      <form v-if="mode === 'login'" class="form" @submit.prevent="submitLogin">
        <label class="field">
          <span>Email</span>
          <input v-model="email" type="email" required autocomplete="username" />
        </label>
        <label class="field">
          <span>Password</span>
          <input v-model="password" type="password" required autocomplete="current-password" />
        </label>
        <p v-if="error" class="error">{{ error }}</p>
        <button type="submit" class="primary" :disabled="loading">
          {{ loading ? 'Signing in…' : 'Sign in' }}
        </button>
      </form>

      <form v-else class="form" @submit.prevent="submitRegister">
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
        <label class="field">
          <span>Address</span>
          <input v-model="address" required autocomplete="street-address" />
        </label>
        <p v-if="error" class="error">{{ error }}</p>
        <button type="submit" class="primary" :disabled="loading">
          {{ loading ? 'Creating…' : 'Create account' }}
        </button>
      </form>
    </div>
  </div>
</template>

<style scoped>
.auth-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 2rem;
  background: linear-gradient(145deg, #faf7f5 0%, #e8e0db 45%, #d4c4bc 100%);
}

.panel {
  width: 100%;
  max-width: 400px;
  background: #fff;
  border-radius: 12px;
  padding: 2rem 2.25rem;
  box-shadow: 0 20px 50px rgba(40, 32, 28, 0.12);
}

.brand {
  font-family: 'Georgia', 'Times New Roman', serif;
  font-size: 2rem;
  font-weight: 400;
  letter-spacing: 0.08em;
  margin: 0;
  color: #2c2420;
}

.tagline {
  margin: 0.25rem 0 1.5rem;
  font-size: 0.9rem;
  color: #6b5e58;
}

.tabs {
  display: flex;
  gap: 0;
  margin-bottom: 1.5rem;
  border-radius: 8px;
  overflow: hidden;
  border: 1px solid #e5ded8;
}

.tabs button {
  flex: 1;
  padding: 0.65rem 1rem;
  border: none;
  background: #f5f0ec;
  color: #5c524d;
  font-size: 0.95rem;
  cursor: pointer;
  transition: background 0.15s, color 0.15s;
}

.tabs button.active {
  background: #2c2420;
  color: #faf7f5;
}

.form {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.field {
  display: flex;
  flex-direction: column;
  gap: 0.35rem;
  font-size: 0.85rem;
  color: #4a423e;
}

.field span {
  font-weight: 500;
}

.field input,
.field select {
  padding: 0.6rem 0.75rem;
  border: 1px solid #d9d0c9;
  border-radius: 6px;
  font-size: 1rem;
  background: #fff;
}

.field input:focus,
.field select:focus {
  outline: 2px solid #8b7355;
  outline-offset: 1px;
  border-color: #8b7355;
}

.primary {
  margin-top: 0.5rem;
  padding: 0.75rem 1rem;
  border: none;
  border-radius: 6px;
  background: #2c2420;
  color: #faf7f5;
  font-size: 1rem;
  cursor: pointer;
}

.primary:disabled {
  opacity: 0.65;
  cursor: not-allowed;
}

.error {
  margin: 0;
  font-size: 0.85rem;
  color: #a42020;
}
</style>
