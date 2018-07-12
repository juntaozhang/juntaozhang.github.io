
curl upload file
```
curl --connect-timeout 1800 -m 1800 -H "Expect:" -F "user_id=admin" -F "path=123.tar.gz" -F "overwrite=1" -F "files=@123.tar.gz" http://10.5.1.45:20100/mobeye/upload3/

curl --connect-timeout 1800 -m 1800 -H "Expect:" -F "module=zhangjt"  -F "path=123.tar.gz" -F "file=@123.tar.gz" http://10.5.1.45:20101/fs/upload/
```