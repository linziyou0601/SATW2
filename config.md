# 網站環境說明文件

## 目錄結構範例

    /var/www/
          ├─ satw2bot/
          |   ├─ app.py                  #主程式
          |   ├─ env.py                  #環境變數函式
          |   ├─ model.py                #資料庫操作函式
          |   ├─ Controllers/
          |   |  ├─ normalController.py     #主要功能指令函式
          |   |  ├─ chatterController.py    #聊天功能相關指令函式
          |   |  └─ keyController.py        #關鍵字回應類型篩型指令函式
          |   ├─ Services/
          |   |  ├─ autoLearnServices.py    #聊天自動學習模組
          |   |  ├─ crawlerService.py       #聊天自動學習模組
          |   |  └─ geocodingService.py     #聊天自動學習模組
          |   ├─ Others/
          |   |  └─ flexMessageJSON.py      #FLEX MESSAGE物件
          |   └─ uwsgi/
          |       ├─ uwsgi.ini        #組態檔
          |       ├─ uwsgi.status     #自動產生 (查詢uwsgi程式之狀態)
          |       ├─ uwsgi.pid        #自動產生 (查詢uwsgi程式之pid)
          |       ├─ uwsgi.log        #自動產生 (查詢uwsgi程式之log entry)
          |       └─ uwsgi.sock       #自動產生 (Apache、nginx接入點)
          └─ html/ 
              └─ satw2/
                  ├─ bin
                  ├─ src
                  ├─ build
                  ├─ build.gradle
                  ├─ gradle
                  ├─ gradlew
                  ├─ gradlew.bat
                  ├─ README.md
                  └─ settings.gradle

## 基本參數

### Host

    Domain: satw2.linziyou.nctu.me
    IP Address: {ON-GOOGLE-DHCP}

### Ports

    satw2: 
        TCP 443  allow  (HTTPS)
        TCP 8087 allow  (HTTP)

    satw2bot:
        TCP 4567 allow  (HTTPS)
        TCP 5000 deny   (HTTP)

### 監聽 port 80, 443 `/etc/apache2/ports.conf`

```apacheconf
Listen 80

<IfModule ssl_module>
    Listen 443
</IfModule>
<IfModule mod_gnutls.c>
    Listen 443
</IfModule>

#bot
<IfModule ssl_module>
    Listen 4567
</IfModule>
<IfModule mod_gnutls.c>
    Listen 4567
</IfModule>

#tomcat
<IfModule ssl_module>
    Listen 8080
</IfModule>
<IfModule mod_gnutls.c>
    Listen 8080
</IfModule>

#phpMyadmin
<IfModule ssl_module>
    Listen 8888
</IfModule>
<IfModule mod_gnutls.c>
    Listen 8888
</IfModule>
```

### 網站目錄組態 `/etc/apache2/apache2.conf`

```apacheconf
<Directory /var/www/html/>
    Options FollowSymLinks
    AllowOverride all
    Require all granted
</Directory>
```

### VirtualHost配置 `/etc/apache2/sites-available/000-default-le-ssl.conf`

```apacheconf
#phpmyadmin
<VirtualHost *:8888>
        SSLEngine On
        SSLCertificateFile /etc/letsencrypt/live/satw2.linziyou.nctu.me/cert.pem
        SSLCertificateKeyFile /etc/letsencrypt/live/satw2.linziyou.nctu.me/privkey.pem
        SSLCertificateChainFile /etc/letsencrypt/live/satw2.linziyou.nctu.me/fullchain.pem
        Alias /phpmyadmin /usr/share/phpmyadmin
</VirtualHost>

#web
<VirtualHost *:443>
    SSLEngine On
        # SSL
        SSLCertificateFile /etc/letsencrypt/live/satw2.linziyou.nctu.me/cert.pem
        SSLCertificateKeyFile /etc/letsencrypt/live/satw2.linziyou.nctu.me/privkey.pem
        SSLCertificateChainFile /etc/letsencrypt/live/satw2.linziyou.nctu.me/fullchain.pem
        # 網站設定
        ServerName satw2.linziyou.nctu.me
        ServerAlias satw2.linziyou.nctu.me
        # 網站日誌
        ErrorLog ${APACHE_LOG_DIR}/error.log
        CustomLog ${APACHE_LOG_DIR}/access.log combined
        # Proxy
        ProxyPreserveHost on
        RequestHeader set X-Forwarded-Proto https
        RequestHeader set X-Forwarded-Port 443
        ProxyPass / http://127.0.0.1:8087/
        ProxyPassReverse / http://127.0.0.1:8087/
        DocumentRoot /var/www/html
</VirtualHost>

#tomcat
<VirtualHost *:8080>
        SSLEngine On
        SSLCertificateFile /etc/letsencrypt/live/satw2.linziyou.nctu.me/cert.pem
        SSLCertificateKeyFile /etc/letsencrypt/live/satw2.linziyou.nctu.me/privkey.pem
        SSLCertificateChainFile /etc/letsencrypt/live/satw2.linziyou.nctu.me/fullchain.pem

        BrowserMatch ".*MSIE.*" nokeepalive ssl-unclean-shutdown downgrade-1.0 force-response-1.0
        SSLCipherSuite ALL:!ADH:!EXPORT56:RC4+RSA:+HIGH:+MEDIUM:+LOW:+SSLv2:+EXP:+eNULL

        ServerName satw2.linziyou.nctu.me:8080
        ServerAlias satw2.linziyou.nctu.me

        ProxyRequests Off
        ProxyPreserveHost On
        ProxyPass / http://localhost:8081/
        ProxyPassReverse / http://localhost:8081/
</VirtualHost>

#bot
<VirtualHost *:4567>
    # 開啟SSL
    SSLEngine On
    SSLCertificateFile /etc/letsencrypt/live/linziyou.nctu.me/cert.pem
    SSLCertificateKeyFile /etc/letsencrypt/live/linziyou.nctu.me/privkey.pem
    SSLCertificateChainFile /etc/letsencrypt/live/linziyou.nctu.me/fullchain.pem
    ServerName linziyou.nctu.me:4567
    ServerAlias linziyou.nctu.me
    # 代理設定
    ProxyPass / unix:/var/www/coolpanda/uwsgi/uwsgi.sock|uwsgi://127.0.0.1:5000/
</VirtualHost>
```

------------

## 網站配置

### Spring Boot 網站配置

1. 必要安裝項目(apt-get)： `gradle`, `java-14`
2. 必要開啟模組(a2enmod)： `ssl`, `proxy`, `proxy_balancer`, `proxy_http`

### LINE BOT 配置（使用Apache2反向代理uWSGI）

1. 必要安裝項目(apt-get)： `python3.7`, `python3-pip`, `python3.7-dev`, `build-essential`

    > 若出現「ModuleNotFoundError: No module named 'apt_pkg'」，則執行以下指令，複製python3.6的套件給python3.7

        cd /usr/lib/python3/dist-packages/
        sudo cp apt_pkg.cpython-36m-x86_64-linux-gnu.so apt_pkg.cpython-37m-x86_64-linux-gnu.so

2. 以sudo su安裝 (pip3)：`uwsgi`

3. uwsgi安裝完才可安裝(apt-get)： `libapache2-mod-proxy-uwsgi`

4. 必要開啟模組(a2enmod)： `proxy_uwsgi`

------------

## Spring Boot 網站維護

### 備份區塊鏈及圖片

    makir ~/bc
    sudo cp /var/www/html/satw2/bin/main/unverifiedTransactions.json ~/bc
    sudo cp /var/www/html/satw2/bin/main/blockchain.json ~/bc
    mkdir ~/uploads
    sudo cp /var/www/html/satw2/bin/main/blockchain.json ~/bc/stat

### 刪除舊檔

    cd /var/www/html
    sudo rm -rf sa*

### 利用 wget 取得 github上 的 SATW2.zip

    cd /var/www/html
    sudo wget -O satw2.zip https://github.com/linziyou0601/SATW2/archive/master.zip
    sudo unzip satw2.zip
    sudo mv SATW2-master satw2

### 開啟、關閉維護模式(down開啟、up關閉)

    cd /var/www/html/linziyou0601-laravel
    php artisan down
    php artisan up

### 資料夾權限

    cd /var/www/html
    sudo chmod 755 -R linziyou0601-laravel; sudo chown $USER:www-data -R linziyou0601-laravel; sudo chmod 775 -R linziyou0601-laravel/storage linziyou0601-laravel/bootstrap/cache

### 環境變數 `.env` 下的 `DB_PASSWORD` 及 `APP_DEBUG` 要記得改

    sudo vim /var/www/html/linziyou0601-laravel/.env

------------

## LINE BOT 網站維護（uWSGI開關）

### 利用　wget 取得 github上 的 coolpanda.zip

    cd /var/www
    sudo wget -O coolpanda.zip https://github.com/linziyou0601/coolpanda/archive/master.zip
    sudo unzip coolpanda.zip
    sudo mv coolpanda-master coolpanda

### 利用 `uwgsi.pid` 及 `uwgsi.status` 管理程式

    sudo uwsgi --ini /var/www/coolpanda/uwsgi/uwsgi.ini                 //啟動
    sudo tail -f /var/www/coolpanda/uwsgi/uwsgi.log                     //查看log
    sudo uwsgi --stop /var/www/coolpanda/uwsgi/uwsgi.pid                //關閉
    sudo uwsgi --reload /var/www/coolpanda/uwsgi/uwsgi.pid              //重啟
    sudo tail -f /var/log/apache2/access.log                      //apache2 access.log

### 若忘了做 `uwsgi --stop` 可用以下方法關閉（防止佔用 port 5000）

    sudo netstat -tulpn     //查詢佔用port
    ps aux | grep BotApp    //查詢PID
    sudo kill -9 <PID_NUMBER>  //kill掉 BotApp uWSGI 開頭的所有程式
