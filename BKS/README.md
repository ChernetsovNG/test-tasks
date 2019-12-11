1 - Для сборки проекта из папки BKS запускаем команду:

/gradlew clean build

2 - Запускаем собранный jar-файл:

java -jar ./build/libs/*.jar

3 - После этого делаем тестовый запрос через curl:

curl -X POST http://localhost:8080/stocks/allocations 
  -H 'Content-Type: application/json' 
  -d '{"stocks": [{"symbol": "AAPL","volume": 50},
  {"symbol": "HOG","volume": 10},{"symbol": "MDSO","volume": 1},
  {"symbol": "IDRA","volume": 1},{"symbol": "MRSN","volume": 1}]}'