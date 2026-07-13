# EasyQuests ⚔️ [v1.5.5]

EasyQuests to nowoczesny, lekki i niezwykle wydajny plugin na zadania z **linearnym drzewkiem progresji**, stworzony z myślą o serwerach Survival, RPG oraz Skyblock.

## ✨ Główne Funkcje (Features)
* 🗺️ **Płynna Paginacja (Wielostronicowe GUI):** Jeśli Twoja ścieżka ma więcej niż 28 zadań, wtyczka automatycznie podzieli ją na dynamiczne strony.
* 💾 **Hybrydowa Baza Danych:** Wybierz najwygodniejszy zapis: płaskie pliki `YAML`, lokalną i ultraszybką bazę `SQLite` lub zewnętrzną bazę `MySQL` (niezbędną przy sieciach BungeeCord).
* 🛡️ **System Anty-Exploit:** Blokuje oszustwa związane ze stawianiem i niszczeniem w kółko tych samych rud (plugin pamięta bloki postawione przez graczy).
* 🧪 **Wsparcie Alchemii i Rzemiosła:** Dynamiczne śledzenie niszczenia rud (w tym Deepslate), rzemiosła (obsługa Shift-Click) oraz warzenia mikstur (kompatybilne z 1.20.5+).
* ⏱️ **Znikający HUD (Fading HUD):** Paski BossBar i ActionBar pojawiają się tylko wtedy, gdy gracz aktywnie wykonuje zadanie i automatycznie wygaszają się po 5 sekundach bezczynności.

## 🔗 Integracja z CustomNameplates & PlaceholderAPI
Plugin dostarcza zaawansowane i zoptymalizowane zmienne PlaceholderAPI, które możesz wkleić do konfiguracji **CustomNameplates** lub dowolnego Scoreboardu:

| Placeholder | Opis | Przykładowy Wynik |
| :--- | :--- | :--- |
| `%easyquests_has_active%` | Zwraca `true` lub `false` (używane jako warunek wyświetlania HUD) | `true` |
| `%easyquests_active_quest%` | Nazwa obecnie wykonywanego zadania | `Węglowy Początek` |
| `%easyquests_progress%` | Aktualny postęp gracza w zadaniu | `4` |
| `%easyquests_target%` | Wymagana ilość do ukończenia zadania | `16` |
| `%easyquests_percent%` | Procentowe ukończenie misji | `25` |
| `%easyquests_progress_bar%` | RPG-owy, kolorowy pasek postępu z kwadracikami | `■■■■■■■■■■` |

## 🛠️ Komendy i Uprawnienia (Commands & Permissions)

* `/quests` (Aliasy: `/zadania`, `/easyquests`) — Otwiera główne menu kategorii zadań.  
  *Uprawnienie:* `easyquests.use` (Domyślnie: true)
* `/quests reload` — Przeładowuje plik `config.yml` oraz wszystkie pliki w folderze `categories/`.  
  *Uprawnienie:* `easyquests.admin` (Domyślnie: OP)

## 📋 Wymagania (Requirements)
* Silnik serwera: **Paper / Spigot 1.16 - 1.20.x+**
* Wersja Javy: **Java 17 lub nowsza**
* Opcjonalnie: **PlaceholderAPI** (dla integracji z CustomNameplates)
