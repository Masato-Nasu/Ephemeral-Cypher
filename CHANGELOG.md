# Changelog

## 2.0.1

- Replaced the title serif with a restrained sans-serif treatment.
- Removed passage-based key material.
- Added `.txt` export for generated ciphertext and decrypted output.
- Added `.txt` import that fills the ciphertext input and switches to decrypt mode.
- Bumped the PWA cache version.

## 2.0.0

- Removed the third-party URL relay fallback.
- Added separate GOOD / OOPS / UNREACHABLE states.
- Added EC2 envelopes using HKDF-SHA-256 and AES-256-GCM.
- Preserved EC1 and unprefixed legacy decryption.
- Rebuilt the responsive UI and PWA shell.
