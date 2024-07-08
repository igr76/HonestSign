package com.example.honestsign;

import lombok.Getter;

import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class CrptApi {
    private Semaphore semaphore;

    public CrptApi(int requestsLimit, long interval) {
        this.requestsLimit = requestsLimit;
        this.interval = interval;
        this.semaphore = new Semaphore(requestsLimit);
    }

    public void createDocument(Object document, String signature) {
        try {
            semaphore.acquire();

            // Преобразование документа и создание запроса

            try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
                HttpPost httpPost = new HttpPost(apiUrl);

                // Устанавливка заголовка
                httpPost.setHeader("Content-Type", "application/json");

                ObjectMapper objectMapper = new ObjectMapper();
                String documentJson = objectMapper.writeValueAsString(document);

                // Формирование запроса
                String requestBody = String.format("{ \"product_document\": \"%s\", \"document_format\": \"MANUAL\", \"type\": \"LP_INTRODUCE_GOODS\", \"signature\": \"%s\" }", documentJson, signature);
                StringEntity entity = new StringEntity(requestBody);
                httpPost.setEntity(entity);

                try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
                    // Обработка ответа
                    int statusCode = response.getStatusLine().getStatusCode();
                    if (statusCode == 200) {
                        System.out.println("Document successfully created!");
                    } else {
                        System.out.println("Failed to create the document. Status code: " + statusCode);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            // Освобождение семафора после успешного запроса
            semaphore.release();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        CrptApi crptApi = new CrptApi(3, 1000); // Ограничение - 3 запросов в секунду


        for (int i = 0; i < 10; i++) {
            final int documentNumber = i + 1;
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    Description description = new Description("Document description");
                    Product product1 = new Product("certificate 1", "2024-05-28", "AA111", "owner1", "producer1", "2024-05-25", "tnved1", "uit1", "uitu1");
                    Product product2 = new Product("certificate 1", "2023-04-29", "BB222", "owner2", "producer2", "2023-04-24", "tnved2", "uit2", "uitu2");
                    List<Product> products = new ArrayList<>();
                    products.add(product1);
                    products.add(product2);
                    Document document = new Document(description, "1", "approved", "type1", true, "owner_inn", "participant_inn", "producer_inn", "2024-07-04", "type2", products, "2024-07-04", "reg123");
                    String signature = "Подпись";
                    crptApi.createDocument(document, signature + " " + documentNumber);
                }
            });
            thread.start();
        }
    }
}

class Document {

    private Description description;
    private String doc_id;
    private String doc_status;
    private String doc_type;
    private boolean importRequest;
    private String owner_inn;
    private String participant_inn;
    private String producer_inn;
    private String production_date;
    private String production_type;
    private List<Product> products;
    private String reg_date;
    private String reg_number;

    public Document(Description description, String doc_id, String doc_status, String doc_type, boolean importRequest, String owner_inn, String participant_inn, String producer_inn, String production_date, String production_type, List<Product> products, String reg_date, String reg_number) {
        this.description = description;
        this.doc_id = doc_id;
        this.doc_status = doc_status;
        this.doc_type = doc_type;
        this.importRequest = importRequest;
        this.owner_inn = owner_inn;
        this.participant_inn = participant_inn;
        this.producer_inn = producer_inn;
        this.production_date = production_date;
        this.production_type = production_type;
        this.products = products;
        this.reg_date = reg_date;
        this.reg_number = reg_number;
    }
    class Product {
        private String certificate_document;
        private String certificate_document_date;
        private String certificate_document_number;
        private String owner_inn;
        private String producer_inn;
        private String production_date;
        private String tnved_code;
        private String uit_code;
        private String uitu_code;

        public Product(String certificate_document, String certificate_document_date, String certificate_document_number, String owner_inn, String producer_inn, String production_date, String tnved_code, String uit_code, String uitu_code) {
            this.certificate_document = certificate_document;
            this.certificate_document_date = certificate_document_date;
            this.certificate_document_number = certificate_document_number;
            this.owner_inn = owner_inn;
            this.producer_inn = producer_inn;
            this.production_date = production_date;
            this.tnved_code = tnved_code;
            this.uit_code = uit_code;
            this.uitu_code = uitu_code;
        }
}
