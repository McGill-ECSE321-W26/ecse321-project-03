<script setup>
import { onMounted, ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { getAccountById, loginWithEmailPassword, updateAccount, deleteAccount } from '../api/account'
import { getSession, saveSession, clearSession } from '../session/storage'

const emit = defineEmits(['session-updated'])

const router = useRouter()

const session = computed(() => getSession())

const loading = ref(true)
const loadError = ref('')

const initial = getSession()
const username = ref(initial?.username ?? '')
const email = ref('')
const address = ref('')
/** plaintext password when unlocked; used for display + submit */
const passwordField = ref('')

const verifyPasswordInput = ref('')
const verifyLoading = ref(false)
const verifyError = ref('')

const unlocked = ref(false)

const saveLoading = ref(false)
const saveError = ref('')
const saveSuccess = ref('')

const loyaltyPts = ref(50)
const accountType = ref('CUSTOMER')

// ── Delete account state ──────────────────────────────────────────
const deleteStep = ref('idle')   // 'idle' | 'confirm' | 'deleting'
const deleteError = ref('')

async function loadAccount() {
  const s = session.value
  if (!s) {
    await router.replace({ name: 'login' })
    return
  }
  loading.value = true
  loadError.value = ''
  try {
    const acc = await getAccountById(s.accountId)
    username.value = acc.username ?? ''
    email.value = acc.email ?? ''
    address.value = acc.address ?? ''
    loyaltyPts.value = acc.loyaltyPts ?? 0
    accountType.value = acc.accountType ?? s.accountType ?? 'CUSTOMER'
    passwordField.value = ''
  } catch (e) {
    loadError.value = e.message ?? 'Could not load account'
  } finally {
    loading.value = false
  }
}


async function submitVerify() {
  const s = session.value
  if (!s) return
  verifyError.value = ''
  verifyLoading.value = true
  try {
    await loginWithEmailPassword(s.email, verifyPasswordInput.value)
    passwordField.value = verifyPasswordInput.value
    unlocked.value = true
    verifyPasswordInput.value = ''
  } catch (e) {
    verifyError.value = e.message ?? 'Incorrect password'
  } finally {
    verifyLoading.value = false
  }
}

async function submitSave() {
  const s = session.value
  if (!s) return
  saveError.value = ''
  saveSuccess.value = ''
  saveLoading.value = true
  try {
    const updated = await updateAccount(s.accountId, {
      username: username.value.trim(),
      email: email.value.trim(),
      password: passwordField.value,
      address: address.value.trim(),
      accountType: accountType.value,
    })
    saveSession(updated)
    emit('session-updated')
    loyaltyPts.value = updated.loyaltyPts ?? loyaltyPts.value
    saveSuccess.value = 'Your account was updated successfully.'
  } catch (e) {
    saveError.value = e.message ?? 'Could not update account'
  } finally {
    saveLoading.value = false
  }
}

// ── Delete account handlers ───────────────────────────────────────
function requestDelete() {
  deleteError.value = ''
  deleteStep.value = 'confirm'
}

function cancelDelete() {
  deleteError.value = ''
  deleteStep.value = 'idle'
}

async function confirmDelete() {
  const s = session.value
  if (!s) return
  deleteError.value = ''
  deleteStep.value = 'deleting'
  try {
    await deleteAccount(s.accountId)
    clearSession()
    await router.replace({ name: 'login' })
  } catch (e) {
    deleteError.value = e.message ?? 'Could not delete account. Please try again.'
    deleteStep.value = 'confirm'
  }
}

onMounted(loadAccount)
</script>

<template>
  <div class="account-embed">
    <main class="content">
      <p v-if="loadError" class="banner banner--error" role="alert">{{ loadError }}</p>
      <p v-else-if="loading" class="loading-msg">Loading your account…</p>

      <template v-else>
        <h1 class="greeting">
          Hi {{ username }}! <span class="wand" aria-hidden="true">🪄✨</span>
        </h1>
        <p class="intro">
          Thank you for buying with Papa's Fashionistaria! You can view and change your account details here.
        </p>

        <section class="verify-panel" aria-label="Password verification">
          <p class="verify-help">
            To make modifications, please enter the current password and hit submit.
          </p>
          <form class="verify-row" @submit.prevent="submitVerify">
            <label class="verify-label" for="verify-password">password:</label>
            <input
              id="verify-password"
              v-model="verifyPasswordInput"
              type="password"
              class="input-pill"
              autocomplete="current-password"
              :disabled="unlocked || verifyLoading"
            />
            <button type="submit" class="btn-pill btn-pink" :disabled="unlocked || verifyLoading">
              {{ verifyLoading ? '…' : 'submit' }}
            </button>
          </form>
          <p v-if="verifyError" class="field-error" role="alert">{{ verifyError }}</p>
          <p v-if="unlocked" class="verified-note">You can edit your details below.</p>
        </section>

        <section class="details-panel" aria-label="Your account details">
          <h2 class="details-title">YOUR ACCOUNT DETAILS:</h2>

          <div class="details-grid">
            <span class="detail-label">username:</span>
            <input
              v-model="username"
              type="text"
              class="input-pill"
              :readonly="!unlocked"
              :class="{ locked: !unlocked }"
              autocomplete="username"
            />

            <span class="detail-label">password:</span>
            <input
              v-if="unlocked"
              v-model="passwordField"
              type="text"
              class="input-pill"
              autocomplete="new-password"
            />
            <div v-else class="input-pill ghost" aria-hidden="true">*********</div>

            <span class="detail-label">email:</span>
            <input
              v-model="email"
              type="email"
              class="input-pill"
              :readonly="!unlocked"
              :class="{ locked: !unlocked }"
              autocomplete="email"
            />

            <span class="detail-label">address:</span>
            <input
              v-model="address"
              type="text"
              class="input-pill"
              :readonly="!unlocked"
              :class="{ locked: !unlocked }"
              autocomplete="street-address"
            />
          </div>

          <p v-if="saveError" class="field-error" role="alert">{{ saveError }}</p>
          <p v-if="saveSuccess" class="banner banner--ok" role="status">{{ saveSuccess }}</p>

          <div class="details-actions">
            <button
              type="button"
              class="btn-pill btn-pink"
              :disabled="!unlocked || saveLoading"
              @click="submitSave"
            >
              {{ saveLoading ? 'Saving…' : 'Confirm' }}
            </button>
          </div>
        </section>

        <!-- ── Delete Account Section ─────────────────────────────── -->
        <section class="delete-panel" aria-label="Delete account">
          <h2 class="delete-title">DELETE ACCOUNT:</h2>

          <template v-if="deleteStep === 'idle'">
            <p class="delete-help">
              Permanently delete your account and all associated data. This action cannot be undone.
            </p>
            <button
              type="button"
              class="btn-pill btn-danger"
              :disabled="!unlocked"
              @click="requestDelete"
            >
              Delete my account
            </button>
            <p v-if="!unlocked" class="delete-locked-hint">
              Unlock your account above to enable this option.
            </p>
          </template>

          <template v-else-if="deleteStep === 'confirm'">
            <p class="delete-confirm-msg">
              ⚠️ Are you sure? This will permanently delete your account
              <template v-if="accountType === 'EMPLOYEE'">
                and remove all associated roles (employee &amp; customer if applicable)
              </template>. There is no way to recover it.
            </p>
            <p v-if="deleteError" class="field-error" role="alert">{{ deleteError }}</p>
            <div class="delete-actions">
              <button type="button" class="btn-pill btn-ghost" @click="cancelDelete">
                Cancel
              </button>
              <button type="button" class="btn-pill btn-danger" @click="confirmDelete">
                Yes, delete my account
              </button>
            </div>
          </template>

          <template v-else-if="deleteStep === 'deleting'">
            <p class="loading-msg">Deleting your account…</p>
          </template>
        </section>

      </template>
    </main>
  </div>
</template>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Jersey+10&display=swap');

.account-embed {
  color: #3d3c3c;
}

.content {
  max-width: 720px;
  margin: 0 auto;
  padding: 2rem 1.5rem 3rem;
}

.greeting {
  font-family: 'Jersey 10', cursive;
  color: #1209ab;
  font-size: clamp(2rem, 5vw, 3rem);
  font-weight: normal;
  margin: 0 0 0.5rem;
}

.wand {
  font-family: system-ui, sans-serif;
}

.intro {
  font-family: Inconsolata, monospace;
  font-size: 1rem;
  margin: 0 0 1.75rem;
  line-height: 1.5;
}

.verify-panel {
  margin-bottom: 2rem;
}

.verify-help {
  font-family: Inconsolata, monospace;
  font-size: 0.95rem;
  margin: 0 0 0.75rem;
}

.verify-row {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 0.75rem;
}

.verify-label {
  font-family: Inconsolata, monospace;
  font-size: 0.95rem;
}

.input-pill {
  flex: 1;
  min-width: 160px;
  padding: 0.55rem 1.1rem;
  border: 2px solid #3d3c3c;
  border-radius: 9999px;
  font-family: Inconsolata, monospace;
  font-size: 1rem;
  background: #fff;
  box-shadow: inset 0 2px 4px rgba(0, 0, 0, 0.06);
}

.input-pill:focus {
  outline: 2px solid #1209ab;
  outline-offset: 1px;
}

.input-pill.locked {
  background: #f7f7f7;
  color: #555;
  cursor: default;
}

.input-pill.ghost {
  display: flex;
  align-items: center;
  margin: 0;
  cursor: default;
  user-select: none;
}

.btn-pill {
  padding: 0.55rem 1.4rem;
  border-radius: 9999px;
  border: 2px solid #3d3c3c;
  font-family: Inconsolata, monospace;
  font-size: 1rem;
  cursor: pointer;
  box-shadow: 0 3px 0 rgba(0, 0, 0, 0.12);
  transition: transform 0.12s, box-shadow 0.12s;
}

.btn-pill:hover:not(:disabled) {
  transform: translateY(-1px);
}

.btn-pill:disabled {
  opacity: 0.55;
  cursor: not-allowed;
  box-shadow: none;
}

.btn-pink {
  background: linear-gradient(180deg, #ffe5f9 0%, #f5cce8 100%);
  color: #3d3c3c;
  font-weight: 600;
}

.btn-ghost {
  background: #fff;
  color: #3d3c3c;
  font-weight: 600;
}

.btn-danger {
  background: linear-gradient(180deg, #ffe5e5 0%, #f5cccc 100%);
  border-color: #a42020;
  color: #7f1d1d;
  font-weight: 600;
}

.btn-danger:hover:not(:disabled) {
  background: linear-gradient(180deg, #ffd5d5 0%, #f0b8b8 100%);
}

.field-error {
  color: #a42020;
  font-family: Inconsolata, monospace;
  font-size: 0.9rem;
  margin: 0.5rem 0 0;
}

.verified-note {
  font-family: Inconsolata, monospace;
  color: #2d6a4f;
  margin: 0.5rem 0 0;
  font-size: 0.9rem;
}

.details-panel {
  background: #fdf0f6;
  border: 2px solid #b8a8d9;
  border-radius: 12px;
  padding: 1.25rem 1.5rem 1.5rem;
}

.details-title {
  font-family: Inconsolata, monospace;
  font-size: 1.05rem;
  text-decoration: underline;
  margin: 0 0 1rem;
  font-weight: 700;
}

.details-grid {
  display: grid;
  grid-template-columns: minmax(100px, 140px) 1fr;
  gap: 0.75rem 1rem;
  align-items: center;
}

.detail-label {
  font-family: Inconsolata, monospace;
  font-size: 0.95rem;
  text-align: right;
  font-weight: 600;
}

.details-actions {
  display: flex;
  justify-content: flex-end;
  margin-top: 1.25rem;
}

/* ── Delete panel ───────────────────────────────────────────────── */
.delete-panel {
  margin-top: 2.5rem;
  background: #fff5f5;
  border: 2px solid #e8b4b8;
  border-radius: 12px;
  padding: 1.25rem 1.5rem 1.5rem;
}

.delete-title {
  font-family: Inconsolata, monospace;
  font-size: 1.05rem;
  text-decoration: underline;
  margin: 0 0 0.75rem;
  font-weight: 700;
  color: #7f1d1d;
}

.delete-help {
  font-family: Inconsolata, monospace;
  font-size: 0.95rem;
  color: #5a2020;
  margin: 0 0 1rem;
  line-height: 1.5;
}

.delete-locked-hint {
  font-family: Inconsolata, monospace;
  font-size: 0.85rem;
  color: #888;
  margin: 0.5rem 0 0;
}

.delete-confirm-msg {
  font-family: Inconsolata, monospace;
  font-size: 0.95rem;
  color: #5a2020;
  margin: 0 0 1rem;
  line-height: 1.6;
  background: #fde8e8;
  border: 1px solid #f5cccc;
  border-radius: 8px;
  padding: 0.75rem 1rem;
}

.delete-actions {
  display: flex;
  gap: 0.75rem;
  flex-wrap: wrap;
  justify-content: flex-end;
  margin-top: 0.5rem;
}

.banner {
  padding: 0.75rem 1rem;
  border-radius: 8px;
  font-family: Inconsolata, monospace;
}

.banner--error {
  background: #fde8e8;
  color: #7f1d1d;
}

.banner--ok {
  margin-top: 0.75rem;
  background: #e8f5e9;
  color: #1b5e20;
}

.loading-msg {
  font-family: Inconsolata, monospace;
  text-align: center;
  margin-top: 2rem;
}

@media (max-width: 560px) {
  .details-grid {
    grid-template-columns: 1fr;
  }

  .detail-label {
    text-align: left;
  }

  .delete-actions {
    flex-direction: column-reverse;
  }
}
</style>