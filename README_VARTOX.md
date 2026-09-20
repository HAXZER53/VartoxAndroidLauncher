# VartoxMC Android Launcher

Мобильный клиент Minecraft Java Edition для сервера **VartoxMC** (на базе PojavLauncher Core).

## Особенности интеграции
- Синхронизация профилей и обновлений с **LaunchServer** (`http://haxzer.online:9274/` / `ws://haxzer.online:9274/api`).
- Авторизация через базу/API VartoxMC.
- Подгрузка скинов через Vartox Skins API (`http://vartox.online:3000/api/skin`).
- Поддержка сборок с модами: **HiTech** (NeoForge 1.21.1) и **TechnoMagic**.

## Как собрать APK через GitHub Actions:
1. Создайте репозиторий на GitHub (например, `VartoxAndroidLauncher`).
2. Запушьте эту папку в ваш репозиторий:
   ```bash
   git remote add origin https://github.com/<ваш-аккаунт>/VartoxAndroidLauncher.git
   git branch -M main
   git push -u origin main
   ```
3. Перейдите во вкладку **Actions** в вашем GitHub репозитории.
4. Выберите workflow **Android CI** и запустите сборку (`Run workflow`).
5. После завершения сборки во вкладке **Artifacts** появится готовый установочный файл: **`app-debug.apk`**!
