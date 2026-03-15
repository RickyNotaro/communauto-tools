<template>
  <div class="home-page">
    <!-- Hero Section -->
    <section class="hero">
      <div class="hero-content">
        <div class="hero-icon">
          <svg viewBox="0 0 64 64" width="72" height="72" fill="none" xmlns="http://www.w3.org/2000/svg">
            <rect width="64" height="64" rx="16" fill="#198754" />
            <path d="M16 38c0-6 4-14 16-18 12 4 16 12 16 18" stroke="#fff" stroke-width="3" stroke-linecap="round" />
            <circle cx="24" cy="40" r="5" stroke="#fff" stroke-width="2.5" />
            <circle cx="40" cy="40" r="5" stroke="#fff" stroke-width="2.5" />
            <circle cx="24" cy="40" r="1.5" fill="#fff" />
            <circle cx="40" cy="40" r="1.5" fill="#fff" />
            <rect x="28" y="28" width="8" height="5" rx="1.5" fill="#fff" opacity="0.7" />
          </svg>
        </div>
        <h1 class="hero-title">Communauto Tools</h1>
        <p class="hero-subtitle">
          Trouvez et réservez des véhicules Communauto en temps réel à Montréal.
          <br class="d-none d-md-inline" />
          Une interface rapide et intuitive pour le service d'autopartage.
        </p>
        <div class="hero-notice">
          <svg viewBox="0 0 20 20" fill="currentColor" width="16" height="16">
            <path fill-rule="evenodd" d="M8.257 3.099c.765-1.36 2.722-1.36 3.486 0l5.58 9.92c.75 1.334-.213 2.98-1.742 2.98H4.42c-1.53 0-2.493-1.646-1.743-2.98l5.58-9.92zM11 13a1 1 0 11-2 0 1 1 0 012 0zm-1-8a1 1 0 00-1 1v3a1 1 0 002 0V6a1 1 0 00-1-1z" clip-rule="evenodd"/>
          </svg>
          Fonctionne uniquement en local (<code>npm run dev</code>) — Communauto bloque les requêtes via Cloudflare Workers / corsproxy.io.
        </div>
        <div class="hero-actions">
          <router-link to="/vehicules" class="btn btn-light btn-lg me-3">
            <svg class="btn-icon" viewBox="0 0 20 20" fill="currentColor" width="20" height="20">
              <path d="M3 4a1 1 0 011-1h12a1 1 0 011 1v2a1 1 0 01-1 1H4a1 1 0 01-1-1V4zm0 6a1 1 0 011-1h12a1 1 0 011 1v2a1 1 0 01-1 1H4a1 1 0 01-1-1v-2zm0 6a1 1 0 011-1h6a1 1 0 011 1v2a1 1 0 01-1 1H4a1 1 0 01-1-1v-2z"/>
            </svg>
            Véhicules
          </router-link>
          <router-link to="/radar" class="btn btn-outline-light btn-lg">
            <svg class="btn-icon" viewBox="0 0 20 20" fill="currentColor" width="20" height="20">
              <path fill-rule="evenodd" d="M5.05 4.05a7 7 0 119.9 9.9L10 18.9l-4.95-4.95a7 7 0 010-9.9zM10 11a2 2 0 100-4 2 2 0 000 4z" clip-rule="evenodd"/>
            </svg>
            Radar
          </router-link>
        </div>
      </div>
    </section>

    <!-- Features Section -->
    <section class="features container">
      <div class="row g-4">
        <div class="col-md-4">
          <div class="feature-card">
            <div class="feature-icon feature-icon--list">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="32" height="32">
                <path d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2" stroke-linecap="round" stroke-linejoin="round"/>
                <path d="M9 12h6M9 16h6" stroke-linecap="round"/>
              </svg>
            </div>
            <h3>Liste des véhicules</h3>
            <p>
              Consultez tous les véhicules disponibles en temps réel. Triez par distance,
              niveau de batterie ou marque.
            </p>
          </div>
        </div>
        <div class="col-md-4">
          <div class="feature-card">
            <div class="feature-icon feature-icon--map">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="32" height="32">
                <circle cx="12" cy="10" r="3" />
                <path d="M12 21.7C17.3 17 20 13 20 10a8 8 0 10-16 0c0 3 2.7 7 8 11.7z" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
            </div>
            <h3>Carte interactive</h3>
            <p>
              Visualisez les véhicules sur une carte OpenStreetMap avec votre position
              et un rayon de recherche configurable.
            </p>
          </div>
        </div>
        <div class="col-md-4">
          <div class="feature-card">
            <div class="feature-icon feature-icon--bolt">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="32" height="32">
                <path d="M13 2L3 14h9l-1 8 10-12h-9l1-8z" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
            </div>
            <h3>Réservation rapide</h3>
            <p>
              Réservez un véhicule en un clic. Activez le mode radar pour réserver
              automatiquement dès qu'un véhicule apparaît.
            </p>
          </div>
        </div>
      </div>
    </section>

    <!-- How it works -->
    <section class="how-it-works container">
      <h2>Comment ça marche</h2>
      <div class="steps">
        <div class="step">
          <div class="step-number">1</div>
          <div class="step-text">
            <strong>Connectez-vous</strong> avec vos identifiants Communauto (cookies ReserveAuto)
          </div>
        </div>
        <div class="step">
          <div class="step-number">2</div>
          <div class="step-text">
            <strong>Parcourez</strong> les véhicules disponibles en liste ou sur la carte
          </div>
        </div>
        <div class="step">
          <div class="step-number">3</div>
          <div class="step-text">
            <strong>Réservez</strong> votre véhicule directement depuis l'application
          </div>
        </div>
      </div>
    </section>

    <!-- Tech Stack -->
    <section class="tech-stack container">
      <h2>Technologies</h2>
      <div class="tech-badges">
        <span class="tech-badge">Vue 3</span>
        <span class="tech-badge">TypeScript</span>
        <span class="tech-badge">Vite</span>
        <span class="tech-badge">Leaflet</span>
        <span class="tech-badge">Bootstrap 5</span>
        <span class="tech-badge">Axios</span>
      </div>
    </section>

    <footer class="home-footer">
      <p>
        Projet open-source &mdash;
        <a href="https://github.com" target="_blank" rel="noopener noreferrer">GitHub</a>
      </p>
    </footer>
  </div>
</template>

<style scoped lang="scss">
.home-page {
  text-align: center;
}

/* ── Hero ─────────────────────────────────────────── */
.hero {
  background: linear-gradient(135deg, #198754 0%, #0d6e3f 50%, #0a5c34 100%);
  color: #fff;
  padding: 5rem 1.5rem 4rem;
  margin-top: -8px;
}

.hero-content {
  max-width: 700px;
  margin: 0 auto;
}

.hero-icon {
  margin-bottom: 1.5rem;
}

.hero-title {
  font-size: 2.8rem;
  font-weight: 800;
  letter-spacing: -0.02em;
  margin-bottom: 1rem;
}

.hero-subtitle {
  font-size: 1.15rem;
  opacity: 0.92;
  line-height: 1.7;
  margin-bottom: 2.2rem;
}

.hero-notice {
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
  background: rgba(255, 193, 7, 0.15);
  border: 1px solid rgba(255, 193, 7, 0.4);
  color: #fff;
  padding: 0.5rem 1rem;
  border-radius: 8px;
  font-size: 0.9rem;
  margin-bottom: 1.5rem;

  code {
    background: rgba(255, 255, 255, 0.15);
    padding: 0.1rem 0.35rem;
    border-radius: 4px;
    font-size: 0.85rem;
  }
}

.hero-actions {
  display: flex;
  justify-content: center;
  flex-wrap: wrap;
  gap: 0.75rem;

  .btn {
    display: inline-flex;
    align-items: center;
    gap: 0.5rem;
    font-weight: 600;
  }

  .btn-light {
    color: #198754;
  }
}

.btn-icon {
  flex-shrink: 0;
}

/* ── Features ─────────────────────────────────────── */
.features {
  padding: 4rem 1rem;
}

.feature-card {
  background: #fff;
  border: 1px solid #e9ecef;
  border-radius: 12px;
  padding: 2rem 1.5rem;
  height: 100%;
  transition: transform 0.2s, box-shadow 0.2s;

  &:hover {
    transform: translateY(-4px);
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.08);
  }

  h3 {
    font-size: 1.2rem;
    font-weight: 700;
    margin-bottom: 0.75rem;
    color: #2c3e50;
  }

  p {
    color: #6c757d;
    line-height: 1.6;
    margin: 0;
  }
}

.feature-icon {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 1.25rem;

  &--list {
    background: #e8f5e9;
    color: #198754;
  }

  &--map {
    background: #e3f2fd;
    color: #0d6efd;
  }

  &--bolt {
    background: #fff3e0;
    color: #f57c00;
  }
}

/* ── How it works ─────────────────────────────────── */
.how-it-works {
  padding: 3rem 1rem 4rem;

  h2 {
    font-weight: 700;
    margin-bottom: 2.5rem;
    color: #2c3e50;
  }
}

.steps {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
  max-width: 520px;
  margin: 0 auto;
  text-align: left;
}

.step {
  display: flex;
  align-items: center;
  gap: 1.25rem;
}

.step-number {
  flex-shrink: 0;
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: #198754;
  color: #fff;
  font-weight: 700;
  font-size: 1.1rem;
  display: flex;
  align-items: center;
  justify-content: center;
}

.step-text {
  color: #495057;
  line-height: 1.5;
}

/* ── Tech Stack ───────────────────────────────────── */
.tech-stack {
  padding: 2rem 1rem 4rem;

  h2 {
    font-weight: 700;
    margin-bottom: 1.5rem;
    color: #2c3e50;
  }
}

.tech-badges {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 0.75rem;
}

.tech-badge {
  background: #f1f3f5;
  color: #495057;
  padding: 0.45rem 1rem;
  border-radius: 999px;
  font-size: 0.9rem;
  font-weight: 600;
  border: 1px solid #dee2e6;
}

/* ── Footer ───────────────────────────────────────── */
.home-footer {
  padding: 2rem 1rem;
  border-top: 1px solid #e9ecef;
  color: #6c757d;
  font-size: 0.9rem;

  a {
    color: #198754;
    text-decoration: none;
    font-weight: 600;

    &:hover {
      text-decoration: underline;
    }
  }
}
</style>
