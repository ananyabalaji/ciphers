# Stream Cipher and Block Cipher with CBC Implementations
implementation of the stream cipher, a simulation of the one-time pad by using a keystream generator, and a block (16-byte) encryption with cipher block chaining and padding. 

Stream Cipher
simulation of the one-time pad using a pseudorandom number generator to generate keystream data that is the same length as the message
- implements linear congruential keystream generator (with modulus m = 256, multiplier a = 1103515245, increment c = 12345)
- converts the user-provided password into a seed with a sbdm hash
- apply the stream cipher (ciphertxt = plaintxt XOR keytxt)

Usage: 
Encrypt: scrypt password plaintext ciphertext
Decrypt: scrypt password plaintext cipher
