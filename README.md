# EasyQuests ⚔️

[![Version](https://img.shields.io/badge/version-1.2-brightgreen.svg)](https://github.com/twoj-profil/EasyQuests)
[![Java](https://img.shields.io/badge/Java-17%20%2F%2021-orange.svg)](https://www.oracle.com/java/)
[![Platform](https://img.shields.io/badge/Platform-Paper%20%2F%20Spigot-blue.svg)](https://papermc.io)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

**EasyQuests** to nowoczesny, lekki i wysoce konfigurowalny plugin na zadania (questy) stworzony z myślą o serwerach **Survival**. Oferuje graczom głęboki system progresji, a administratorom pełną kontrolę nad interfejsem i ekonomią serwera, zachowując przy tym najwyższe standardy wydajności.

---

## 🌟 Kluczowe Funkcje

*   **📈 Linearne Drzewko Progresji:** Zadania odblokowują się sekwencyjnie. Kolejne wyzwania są zablokowane do momentu ukończenia poprzednich, co eliminuje pomijanie etapów gry.
*   **🎨 Pełne wsparcie dla Hex (RGB):** Wszystkie napisy, wiadomości, paski ActionBar oraz interfejsy GUI obsługują nowoczesne barwy w formacie `&#HEXHEX`.
*   **🔕 Bezspamowy ActionBar:** Informacje o postępie w zadaniach są wyświetlane nad ekwipunkiem podręcznym gracza w formie estetycznego paska `[■■■■■□□□]`, nie zaśmiecając czatu głównego.
*   **🎵 Udźwiękowione GUI (Audio-UX):** Intuicyjna nawigacja wspierana klimatycznymi efektami dźwiękowymi (otwieranie menu, błąd kliknięcia na zablokowane zadanie, pomyślne ukończenie).
*   **💾 Dedykowany zapis UUID:** Zapis postępów opiera się na pliku `<UUID>.yml` dla każdego gracza z osobna, co gwarantuje pełne bezpieczeństwo danych.
*   **🕯️ Efekt Połysku (Enchant):** Ukończone zadania w GUI automatycznie zyskują świecenie, ułatwiając szybki podgląd wykonanych misji.

---

## 🛠️ Zalety Techniczne (Dla Administratorów)

*   **🛡️ Ochrona Przed Duplikacją (Custom InventoryHolder):** Interfejsy GUI są oparte o bezpieczną weryfikację instancji klas, a nie na porównywaniu nazw okien. Uniemożliwia to graczom wyciąganie czy kopiowanie przedmiotów z menu w przypadku lagów serwera.
*   **⚡ Pamięć Podręczna RAM (O(1) Caching):** Dane graczy są ładowane do pamięci wyłącznie na czas ich sesji i natychmiast zwalniane po wyjściu, co minimalizuje zapytania dyskowe w trakcie gry.
*   **🧠 Zabezpieczenie Anti-Crash:** Plugin defensywnie weryfikuje nazwy materiałów i dźwięków wprowadzane w plikach konfiguracyjnych. Jeśli popełnisz literówkę, wtyczka zastosuje bezpieczny fallback zamiast przerywać działanie serwera.
*   **🧩 Refleksyjna Zgodność Wsteczna:** Do nakładania świecenia ikon wykorzystujemy mechanizm refleksji (Reflection API), co pozwala na bezbłędne uruchomienie pluginu na wersjach 1.16 - 1.20, jak i najnowszych 1.21+ (gdzie API uległo zmianom).

---

## 📂 Przykładowa Konfiguracja

### Główne ustawienia (`config.yml`)
Pozwala na łatwe zarządzanie wyglądem głównego interfejsu GUI, przypisywaniem slotów, kolorem paska postępu oraz dźwiękami:

```yaml
gui:
  title: "&#ff9900&lZadania &#ffcc00Survival&#ff9900+&#ffff00Działki"
  size: 27
  fill-empty-slots: true
  filler-material: "GRAY_STAINED_GLASS_PANE"
  
  progress-bar:
    char: "■"
    color-completed: "&#55ff55"
    color-uncompleted: "&#ff5555"

  sounds:
    enabled: true
    open-menu: "BLOCK_CHEST_OPEN"
    quest-locked: "ENTITY_VILLAGER_NO"
    quest-unlocked: "BLOCK_NOTE_BLOCK_PLING"
