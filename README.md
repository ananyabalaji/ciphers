# Implementation of a Stream Cipher and Block Cipher with CBC

# Stream Cipher
simulation of the one-time pad using a pseudorandom number generator to generate keystream data that is the same length as the message
- implements linear congruential keystream generator (with modulus m = 256, multiplier a = 1103515245, increment c = 12345)
- converts the user-provided password into a seed with a sbdm hash
- apply the stream cipher (ciphertext = plaintext ⊕ keytext)

**Usage: **
Encrypt: scrypt password plaintext ciphertext

Decrypt: scrypt password ciphertext plaintext


# Block Cipher with CBC
modification of the stream cipher to operate on 16-byte blocks instead of bytes.
utilizes substitutions (confusion) and permutations (diffusion) - SP Network
- adds padding of 1 to 16 extra bytes at the end of the file

cipher operation flow:
- start with an IV (initialization vector) derived as the first 16 bytes coming from the keystream.
- for each 16 byte plaintext_block,
    - apply the CBC: temp_block(N) = plaintext_block(N) ⊕ ciphertext_block(N-1), using IV if it is the first block.
    - read 16 bytes from keystream and shuffle plaintext based on the data
    - ciphertext_block(N) = temp_block(N) ⊕ keystream(N)
    - write the ciphertext_block(N) to the file
 
**Usage: **
Encrypt: encrypt password plaintext ciphertext

Decrypt: decrypt password ciphertext plaintext
