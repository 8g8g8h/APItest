
コード概要  

首都圏の電車移動での到着時刻の天気を知らせる

このコードで使っているAPIは以下の3つである。  

①:  
名称:
URL :https://api.trip2.jp/ex/tokyo/v1.0/json?src＝出発駅&dst=到着駅&key=114.184.216.100

②:  
名称:HeartRails Express　駅情報取得API  
URL :http://express.heartrails.com/api/xml?method=getStations  


③:  
名称:OpenWeatherMap    
http://api.openweathermap.org/data/2.5/forecast?lon=※&lat=※&APPID=※&units=metric&mode=xml
