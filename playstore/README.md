Play Store packaging checklist

Files to prepare before publishing:
- App bundle (.aab) produced by `./AndroidClient/build-release-aab.sh` (requires `keystore.properties` and keystore file in project root)
- Feature graphic (1024 x 500) in `playstore/artwork/feature_graphic.png`
- Screenshots for phone/tablet (set in `playstore/screenshots/`)
- App icon (adaptive) already included in `AndroidClient/app/src/main/res/mipmap-anydpi-v26/ic_launcher.xml`
- Store listing metadata in `playstore/metadata/` (title, short description, full description)

Steps to build and verify the signed AAB
1. Create `keystore.properties` in repository root with values matching your keystore. See `AndroidClient/keystore.properties.example`.
2. Place your keystore file (e.g., `keystore.jks`) in repository root (do NOT commit it).
3. Run the build script from repo root:
   `./AndroidClient/build-release-aab.sh`
4. Verify the AAB at `AndroidClient/app/build/outputs/bundle/release/app-release.aab`.
5. Use Google Play Console to upload the AAB, add store listing, screenshots, feature graphic, and submit.

Ensure production Firebase config and backend URLs are set in `AndroidClient/app/build.gradle` release BuildConfig fields before building.
