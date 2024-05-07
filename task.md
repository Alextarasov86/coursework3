### Написать многопоточные программы клиента и сервера.

Для передачи данных используйте классы `Message` и `ConnectionService` из socket-lesson.

## Клиент

1. Соединение с сервером устанавливается, когда запускается клиентская программа.
2. Соединение с сервером разрывается, если пользователь решил завершить работу программы или, если сервер перестал
   отвечать.
3. Клиентская программа завершает работу по желанию пользователя (ввод exit в консоль) или, если сервер перестал
   отвечать.
4. Клиент в отдельном потоке получает данные от пользователя, создает экземпляр Message и отправляет сообщение на
   сервер.
5. Клиент в отдельном потоке получает сообщения от сервера и выводит их в консоль.
6. Пользователь может загрузить на сервер '.txt' файл. Передаются: описание файла (не более N символов) и
   содержимое файла (не более M Mb). Значения N и M задать самостоятельно.
7. Пользователь может запросить '.txt' файл, для получения файла пользователь должен ввести его название.
   Названия и описание доступных файлов присылает сервер.

## Сервер

1. Сервер не разрывает соединение с клиентом по своей инициативе.
2. Соединение с клиентом разрывается, если клиент перестал отвечать.
3. Сервер принимает сообщения от клиентов и рассылает их по всем активным соединениям, кроме отправителя.
4. Каждое соединение клиент-сервер должно обрабатываться в отдельном потоке.
5. Активные соединения хранить в потокобезопасной коллекции или мапе.
6. Если клиент присылает файл и он удовлетворяет всем требованиям, сервер сохраняет его и рассылает остальным клиентам
   информацию о том, что загружен новый файл. Сервер рассылает название файла + описание содержимого.
7. Если клиент запрашивает файл, сервер сначала присылает список имен файлов, потом содержимое выбранного из списка
   файла.