# Ciphers: Encryption + Decryption

## Pad-free Columnar Tranposition Cipher
Can be recreated as a table where the characters of a message is written horizontally across the rows of a rod of specific thickness (represented here by the **key**). Characters of message are read vertically by columns. 

(Example) 
BRAVEHEART on a 3 row x 4 column tablet yields BERRHTAEXVAX, where X represents the padded data ignored by the reader to help complete the table. However, in this implementation, the padding is dispensed (so the length of ciphertext = length of plaintext).

A cipher can be arbitrarily long, and this is enabled in the program.

From there, because the cipher is weak (since all the attacker needs is the table size), we utilize a key.
A key is an alphanumeric string defining table width and the sequence in which columns are read from the table.

Uses the user-provided:
- `[optional]` block size (max size of data read in one iteration - default is 16)
- key (no greater than 512 bytes and enables printable ASCII)
- plaintext/ciphertext file

**Usage:**

Encrypt: `columnTransposeEncryption [-b blocksize] -k key [plaintext]`

Decrypt: `columnTransposeDecryption [-b blocksize] -k key [ciphertext]`

NOTES: 
- key is case-sensitive (Ex: cdDC has column sequence [2, 3, 1, 0])
- can read any form of binary data (.bin, .txt, .jpg)

## Stream Cipher
Simulation of the one-time pad using a pseudorandom number generator to generate keystream data that is the same length as the message:

- Implements linear congruential keystream generator (with modulus m = 256, multiplier a = 1103515245, increment c = 12345).
- Converts the user-provided password into a seed with a SBDM hash.
- Apply the stream cipher (ciphertext = plaintext ⊕ keytext).

**Usage:**

Encrypt: `streamCipher password plaintext ciphertext`

Decrypt: `streamCipher password ciphertext plaintext`

## Block Cipher with CBC Mode

Modification of the stream cipher to operate on 16-byte blocks instead of bytes, utilizing cipher block chaining (CBC) and shuffling bytes within a block. 
- Adds padding of 1 to 16 extra bytes at the end of the file to address partial blocks (16 bytes added if the file is an exact multiple of 16 bytes).

**Cipher Operation Flow:**

- Start with an IV (initialization vector) derived as the first 16 bytes coming from the keystream.
- For each 16-byte plaintext_block:
    - Apply the CBC: `temp_block(N) = plaintext_block(N) ⊕ ciphertext_block(N-1)`, using IV if it is the first block.
    - Read 16 bytes from keystream and shuffle plaintext based on the data.
    - `ciphertext_block(N) = temp_block(N) ⊕ keystream(N)`.
    - Write the `ciphertext_block(N)` to the file.

**Usage:**

Encrypt: `blockCipherEncryption password plaintext ciphertext`

Decrypt: `blockCipherDecryption password ciphertext plaintext`


## Testing:
Compile the programs using javac.

To test:
1. Create a random password. (**REMEMBER to reuse to decrypt**)
2. Use a binary data-based file as plaintext (like .txt, .jpg) to encrypt.
3. Use the encrypted ciphertext file to decrypt to a new file (name of your choice) and see the magic happen!
