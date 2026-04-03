const KEY_ID = 'boutique_accountId'
const KEY_TYPE = 'boutique_accountType'
const KEY_EMAIL = 'boutique_email'
const KEY_USERNAME = 'boutique_username'

/** sessionStorage: survives refresh in this tab; cleared when the tab/window is closed. */
export function saveSession(account) {
  sessionStorage.setItem(KEY_ID, String(account.id))
  sessionStorage.setItem(KEY_TYPE, account.accountType ?? 'CUSTOMER')
  sessionStorage.setItem(KEY_EMAIL, account.email ?? '')
  sessionStorage.setItem(KEY_USERNAME, account.username ?? '')
}

export function clearSession() {
  sessionStorage.removeItem(KEY_ID)
  sessionStorage.removeItem(KEY_TYPE)
  sessionStorage.removeItem(KEY_EMAIL)
  sessionStorage.removeItem(KEY_USERNAME)
}

export function isLoggedIn() {
  const id = sessionStorage.getItem(KEY_ID)
  return id != null && id !== ''
}

export function getSession() {
  if (!isLoggedIn()) return null
  return {
    accountId: Number(sessionStorage.getItem(KEY_ID)),
    accountType: sessionStorage.getItem(KEY_TYPE),
    email: sessionStorage.getItem(KEY_EMAIL),
    username: sessionStorage.getItem(KEY_USERNAME),
  }
}
