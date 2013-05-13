Create an X.509 Certificate using openssl
------------------------------------------
(1) openssl genrsa -des3 -out server.key 1024
(2) openssl req -new -key server.key -out server.csr
(3) openssl x509 -req -days 365 -in server.csr -signkey server.key -out server.crt
(4) openssl x509 -in server.crt -noout -text

To sign a textfile, which creates an SMIME message:

(1) openssl smime -sign -inform pem -inkey private.key -signer server.crt -in test -out test.signed
(2) openssl smime -verify -in test.signed -CAfile server.crt

The SMIME message is in test.signed. Note that JavaMail cannot parse this file as-is: you must add an extra blank line after the first body part separator and before the content text (JavaMail expects a separator between any/no headers and the content).

Create an X.509 Certificate using java keytool
-----------------------------------------------

This tool allows you to specify the valid-from date.

For example, to create a certificate with an alias "noreply":

(1) keytool -genkey -alias noreply -keyalg RSA -keysize 1024 -keystore keystore.jks -dname "CN=localhost" -validity 32767 -startdate "2100/01/01 00:00:00"
(2) keytool -list -alias noreply -v -keystore keystore.jks
(3) keytool -export -rfc -alias noreply -file noreply.crt -keystore keystore.jks

The certificate is exported in plaintext (according to the RFC), into noreply.crt.

To create an SMIME message using openssl, first export the private key from the keystore.

(1) keytool -importkeystore -srckeystore keystore.jks -destkeystore keystore.p12 -deststoretype PKCS12
(2) openssl pkcs12 -in keystore.p12 -out extracted.pem -nodes
(3) (... Edit extracted.pem, and and copy/paste the private key block into noreply.private.key ...) 
(4) openssl smime -sign -inform pem -inkey noreply.private.key -signer noreply.crt -in test.txt -out test.signed
(5) openssl smime -verify -in test.signed -CAfile noreply.crt 

PGP Public Keys
---------------------------
Install the gpg command-line tool.

To generate a new public key / private key pair:  gpg --gen-key

To import a public key block (keyring): gpg --import key.txt

To list all key-pairs in your keyring (stored in ~/.gpg/ directory):  gpg --list-keys  OR  gpg --list-keys <key-id or alias>

To sign a text file with a public key:  gpg --clearsign update.txt  (creates signed file update.txt.asc)

To sign a text file with a different key: gpg --clearsign --default-key E7220D0A update.txt

To verify a signed message (public key must be in keyring):  gpg --verify update.txt.asc

To export a public key (creates public key block to paste into keycert):  gpg --export --armor E7220D0A

To display full fingerprint:  gpg --fingerprint E7220D0A

To add another subkey (signing key) to the public key:
gpg --edit-key E7220D0A
gpg> addkey
...
gpg> save

Any subkeys are included when a public key is exported.

PGP Keys used for test cases
-----------------------------
See below for public/private keypairs used in the signed message testcases.

I exported the private keys used for signing updates using the command: gpg --export-secret-keys --armor <keyid>.

(1) noreply@ripe.net

-----BEGIN PGP PUBLIC KEY BLOCK-----
Version: GnuPG v1.4.12 (Darwin)
Comment: GPGTools - http://gpgtools.org

mQENBFC0yvUBCACn2JKwa5e8Sj3QknEnD5ypvmzNWwYbDhLjmD06wuZxt7Wpgm4+
yO68swuow09jsrh2DAl2nKQ7YaODEipis0d4H2i0mSswlsC7xbmpx3dRP/yOu4WH
2kZciQYxC1NY9J3CNIZxgw6zcghJhtm+LT7OzPS8s3qp+w5nj+vKY09A+BK8yHBN
E+VPeLOAi+D97s+Da/UZWkZxFJHdV+cAzQ05ARqXKXeadfFdbkx0Eq2R0RZm9R+L
A9tPUhtw5wk1gFMsN7c5NKwTUQ/0HTTgA5eyKMnTKAdwhIY5/VDxUd1YprnK+Ebd
YNZh+L39kqoUL6lqeu0dUzYp2Ll7R2IURaXNABEBAAG0I25vcmVwbHlAcmlwZS5u
ZXQgPG5vcmVwbHlAcmlwZS5uZXQ+iQE4BBMBAgAiBQJQtMr1AhsDBgsJCAcDAgYV
CAIJCgsEFgIDAQIeAQIXgAAKCRC7zLstV2OVDdjSCACYAyyWr83Df/zzOWGP+qMF
Vukj8xhaM5f5MGb9FjMKClo6ezT4hLjQ8hfxAAZxndwAXoz46RbDUsAe/aBwdwKB
0owcacoaxUd0i+gVEn7CBHPVUfNIuNemcrf1N7aqBkpBLf+NINZ2+3c3t14k1BGe
xCInxEqHnq4zbUmunCNYjHoKbUj6Aq7janyC7W1MIIAcOY9/PvWQyf3VnERQImgt
0fhiekCr6tRbANJ4qFoJQSM/ACoVkpDvb5PHZuZXf/v+XB1DV7gZHjJeZA+Jto5Z
xrmS5E+HEHVBO8RsBOWDlmWCcZ4k9olxp7/z++mADXPprmLaK8vjQmiC2q/KOTVA
uQENBFC0yvUBCADTYI6i4baHAkeY2lR2rebpTu1nRHbIET20II8/ZmZDK8E2Lwyv
eWold6pAWDq9E23J9xAWL4QUQRQ4V+28+lknMySXbU3uFLXGAs6W9PrZXGcmy/12
pZ+82hHckh+jN9xUTtF89NK/wHh09SAxDa/ST/z/Dj0k3pQWzgBdi36jwEFtHhck
xFwGst5Cv8SLvA9/DaP75m9VDJsmsSwh/6JqMUb+hY71Dr7oxlIFLdsREsFVzVec
YHsKINlZKh60dA/Br+CC7fClBycEsR4Z7akw9cPLWIGnjvw2+nq9miE005QLqRy4
dsrwydbMGplaE/mZc0d2WnNyiCBXAHB5UhmZABEBAAGJAR8EGAECAAkFAlC0yvUC
GwwACgkQu8y7LVdjlQ1GMAgAgUohj4q3mAJPR6d5pJ8Ig5E3QK87z3lIpgxHbYR4
HNaR0NIV/GAt/uca11DtIdj3kBAj69QSPqNVRqaZja3NyhNWQM4OPDWKIUZfolF3
eY2q58kEhxhz3JKJt4z45TnFY2GFGqYwFPQ94z1S9FOJCifL/dLpwPBSKucCac9y
6KiKfjEehZ4VqmtM/SvN23GiI/OOdlHL/xnU4NgZ90GHmmQFfdUiX36jWK99LBqC
RNW8V2MV+rElPVRHev+nw7vgCM0ewXZwQB/bBLbBrayx8LzGtMvAo4kDJ1kpQpip
a/bmKCK6E+Z9aph5uoke8bKoybIoQ2K3OQ4Mh8yiI+AjiQ==
=HQmg
-----END PGP PUBLIC KEY BLOCK-----

-----BEGIN PGP PRIVATE KEY BLOCK-----
Version: GnuPG v1.4.12 (Darwin)
Comment: GPGTools - http://gpgtools.org

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
HvGyqMmyKENitzkODIfMoiPgI4k=
=1brL
-----END PGP PRIVATE KEY BLOCK-----

(2) unread@ripe.net

-----BEGIN PGP PUBLIC KEY BLOCK-----
Version: GnuPG v1.4.12 (Darwin)
Comment: GPGTools - http://gpgtools.org

mQENBFC0yfkBCAC/zYZw2vDpNF2Q7bfoTeTmhEPERzUX3y1y0jJhGEdbp3re4v0i
XWDth4lp9Rr8RimoqQFN2JNFuUWvohiDAT91J+vAG/A67xuTWXACyAPhRtRIFhxS
tBu8h/qEv8yhudhjYfVHu8rUbm59BXzO80KQA4UP5fQeDVwGFbvB+73nF1Pwbg3n
RzgLvKZlxdgV2RdU+DvxabkHgiN0ybcJx3nntL3Do2uZEdkkDDKkN6hkUJY0cFbQ
Oge3AK84huZKnIFq8+NA/vsE3dg3XhbCYUlS4yMe0cvnZrH23lnu4Ubp1KBILHVW
K4vWnMEXcx/T2k4/vpXogZNUH6E3OjtlyjX5ABEBAAG0GVVua25vd24gPHVucmVh
ZEByaXBlLm5ldD6JATgEEwECACIFAlC0yfkCGwMGCwkIBwMCBhUIAgkKCwQWAgMB
Ah4BAheAAAoJEHbKke+BzPl9UasH/1Tc2YZiJHw3yaKvZ8jSXDmZKmO69C7YvgsX
B72w4K6d92vy8dLLreqEpzXKtWB1+K6bLZv6MEdNbvQReG3rw1i2Io7kdsKFn9QC
OeY4OwpzBMZIJGWWXxOLz9Auo9a43xU+wL92/oCqFJrLuuppgOIVkL0pBWRDQYqp
3MqyHdsUOEdd7pwUlGJlfLqa7wmO+r04EG1OBRLBg5p4gVARqDrVMA3ym9KF750T
78Il1eWrceLglI5F0h4RYEmQ3amF/ukbPyzf26+J6MnWeDSO3Q8P/aDO3L7ccNoC
VwyHxUumWgfQVEnt6IaKLSjxVPhhAFO0wLd2tgaUH1y/ug1RgJe5AQ0EULTJ+QEI
APgAjb0YCTRvIdlYFfKQfLCcIbifwFkBjaH9fN8A9ZbeXSWtO7RXEvWF70/ZX69s
1SfQyL4cnIUN7hEd7/Qgx63IXUfNijolbXOUkh+S41tht+4IgJ7iZsELuugvbDEb
VynMXFEtqCXm1zLfd0g2AsWPFRczkj7hWE0gNs7iKvEiGrjFy0eSd/q07oWLxJfq
n4GBBPMGkfKxWhy5AXAkPZp1mc7mlYuNP9xrn76bl69T0E69kDPS3JetSaVWj0Uh
NSJSjP1Zc8g+rvkeum3HKLoW0svRo2XsldjNMlSuWb/oxeaTdGZV6SxTJ+T1oHAi
tovyQHusvGu3D9dfvTcW3QsAEQEAAYkBHwQYAQIACQUCULTJ+QIbDAAKCRB2ypHv
gcz5fe7cB/9PrDR7ybLLmNAuoafsVQRevKG8DfVzDrgThgJz0jJhb1t74qy5xXn+
zW8d/f/JZ8jr7roWA64HKvdvo8ZXuGEf6H20p1+HbjYpT52zteNU/8ljaqIzJBes
tl8ecFB7qg3qUSDQseNaA1uHkZdxGybzgI69QlOyh8fRfOCh/ln9vAiL0tW+Kzjg
8VMY0N3HzBcAPSB7U8wDf1qMzS5Lb1yNunD0Ut5qxCq3fxcdLBk/ZagHmtXoelhH
Bng8TRND/cDUWWH7Rhv64NxUiaKsrM/EmrHFOpJlXuMRRx4FtRPZeXTOln7zTmIL
qqHWqaQHNMKDq0pf24NFrIMLc2iXCSh+
=FPEl
-----END PGP PUBLIC KEY BLOCK-----

-----BEGIN PGP PRIVATE KEY BLOCK-----
Version: GnuPG v1.4.12 (Darwin)
Comment: GPGTools - http://gpgtools.org

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
RayDC3Nolwkofg==
=de59
-----END PGP PRIVATE KEY BLOCK-----

To use these keys, copy/paste into a text file, and use: gpg --import <filename>

The secret key password is: password

