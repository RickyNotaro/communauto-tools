<template>
  <div id="nav">
    <router-link to="/">Accueil</router-link> |
    <router-link to="/vehicules">Véhicules Libres</router-link> |
    <router-link to="/radar">Radar</router-link>
    <span class="auth-section">
      <template v-if="isAuthenticated">
        | <a href="#" @click.prevent="logout">Déconnexion</a>
      </template>
      <template v-else>
        | <a href="#" @click.prevent="showTokenModal = true">Connexion</a>
      </template>
    </span>
  </div>

  <div v-if="showTokenModal" class="modal-backdrop" @click.self="showTokenModal = false">
    <div class="modal-box">
      <h5>Connexion</h5>
      <p class="text-muted small">
        1. Connectez-vous sur
        <a href="https://www.reservauto.net" target="_blank">reservauto.net</a><br>
        2. DevTools (F12) &rarr; Application &rarr; Cookies &rarr; <code>www.reservauto.net</code><br>
        3. Clic droit sur un cookie &rarr; <strong>Copy all cookies</strong> (ou copiez la ligne <code>cookie:</code> d'une requête dans Network)
      </p>

      <label class="form-label small fw-bold">Cookies</label>
      <textarea
        v-model="cookiesInput"
        class="form-control form-control-sm mb-3"
        rows="3"
        placeholder="cf_clearance=...; uid=446674; bid=1; mySession=0060f6fa-..."
      ></textarea>

      <details class="mb-3">
        <summary class="small text-muted">REST API token (optionnel)</summary>
        <p class="text-muted small mt-1">
          Sur <a href="https://quebec.client.reservauto.net" target="_blank">quebec.client.reservauto.net</a>
          &rarr; Local Storage &rarr; clé <code>oidc.user:</code> &rarr; <code>access_token</code>
        </p>
        <textarea v-model="tokenInput" class="form-control form-control-sm" rows="2" placeholder="access_token (optionnel)"></textarea>
      </details>

      <div class="d-flex gap-2 justify-content-end">
        <button class="btn btn-secondary btn-sm" @click="showTokenModal = false">Annuler</button>
        <button class="btn btn-primary btn-sm" :disabled="!cookiesInput.trim()" @click="submitToken">Connecter</button>
      </div>
    </div>
  </div>

  <router-view/>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { useAuth } from '@/composables/useAuth';

const { isAuthenticated, login, logout } = useAuth();

const showTokenModal = ref(false);
const tokenInput = ref('');
const cookiesInput = ref('');

function submitToken() {
  login({
    accessToken: tokenInput.value || undefined,
    cookies: cookiesInput.value,
  });
  tokenInput.value = '';
  cookiesInput.value = '';
  showTokenModal.value = false;
}
</script>

<style lang="scss">
#app {
  font-family: Avenir, Helvetica, Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  text-align: center;
  color: #2c3e50;
}

#nav {
  padding: 30px;

  a {
    font-weight: bold;
    color: #2c3e50;

    &.router-link-exact-active {
      color: #42b983;
    }
  }

  .auth-section a {
    color: #42b983;
    cursor: pointer;
  }
}

.modal-backdrop {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 9999;
}

.modal-box {
  background: #fff;
  border-radius: 8px;
  padding: 1.5rem;
  max-width: 500px;
  width: 90%;
  text-align: left;
}
</style>
