# Implementation of a Stream Cipher and Block Cipher with CBC

## Stream Cipher

Simulation of the one-time pad using a pseudorandom number generator to generate keystream data that is the same length as the message:

- Implements linear congruential keystream generator (with modulus m = 256, multiplier a = 1103515245, increment c = 12345).
- Converts the user-provided password into a seed with a SBDM hash.
- Apply the stream cipher (ciphertext = plaintext ⊕ keytext).

**Usage:**

Encrypt: `streamCipher password plaintext ciphertext`

Decrypt: `streamCipher password ciphertext plaintext`

## Block Cipher with CBC

Modification of the stream cipher to operate on 16-byte blocks instead of bytes, utilizing substitutions (confusion) and permutations (diffusion) - SP Network. 

- Adds padding of 1 to 16 extra bytes at the end of the file.

Cipher operation flow:

- Start with an IV (initialization vector) derived as the first 16 bytes coming from the keystream.
- For each 16-byte plaintext_block:
    - Apply the CBC: `temp_block(N) = plaintext_block(N) ⊕ ciphertext_block(N-1)`, using IV if it is the first block.
    - Read 16 bytes from keystream and shuffle plaintext based on the data.
    - `ciphertext_block(N) = temp_block(N) ⊕ keystream(N)`.
    - Write the `ciphertext_block(N)` to the file.

**Usage:**

Encrypt: `blockCipherEncryption password plaintext ciphertext`

Decrypt: `blockCipherDecryption password ciphertext plaintext`
