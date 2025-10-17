#!/usr/local/anaconda3/envs/py39/bin/python3.9
# coding=utf-8
import base64
import sys

from Crypto.Cipher import AES


def add_to_16(s):
    while len(s) % 16 != 0:
        s += '\0'
    return str.encode(s)


t = sys.argv[1]
key = sys.argv[2]
text = sys.argv[3]

aes = AES.new(str.encode(key), AES.MODE_ECB)
if t == '1':
    encrypted_text = base64.b64encode(aes.encrypt(add_to_16(text)))
    print(encrypted_text.decode("utf-8").replace('\x00', ''))
else:
    decrypted_text = aes.decrypt(base64.b64decode(text))
    print(decrypted_text.decode("utf-8").replace('\x00', ''))
