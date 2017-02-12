# comicdog - 線上漫畫下載

目前只支援下載卡提諾跟動漫狂的漫畫  
bin/下面有可以執行的jar擋

## 更新紀錄

* 2017-02-12
  新增動漫狂漫畫下載

## 使用方法
```java -jar comicdog-0.1.jar <url1> <url2>```

例如: 
```java -jar comicdog-0.1.jar http://comic.ck101.com/comic/6643/1/0/3 http://comic.ck101.com/comic/6643/1/0/2```

## 怎麼新增來源網站
參考src/site/ck101.clj,新增一個src/site/xxx.clj

## TODO
* 新增其他來源
* 支援下載使用JavaScript動態產生的頁面


## License

EPL 1.0

