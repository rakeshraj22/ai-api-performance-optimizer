# AI-Driven API Performance Optimizer

[![GitHub stars](https://img.shields.io/github/stars/rakeshraj22/ai-api-performance-optimizer?style=social)](https://github.com/rakeshraj22/ai-api-performance-optimizer/stargazers)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

This project demonstrates an AI-driven system designed to monitor REST API performance, analyze metrics using AI, and provide optimization suggestions via weekly email reports.





---

## 🚀 Features

* **API Simulation:** A simple Spring Boot application (`performance-demo`) exposes sample REST endpoints, including one with simulated latency and errors.
* **Metrics Collection:** Uses Spring Boot Actuator and Micrometer to expose performance metrics in Prometheus format.
* **Monitoring Stack:**
    * **Prometheus:** Scrapes and stores time-series metrics from the API. 
    * **Grafana:** Visualizes metrics with pre-configured dashboards (optional import). 
* **AI Analysis:** A separate Spring Boot application (`analyzer-service`) runs scheduled tasks to:
    * Query Prometheus for potential performance bottlenecks (high latency, high error rates).
    * Send detected issues to a free, fast AI model (Groq/Llama 3) via an OpenAI-compatible API.
    * Generate human-readable optimization suggestions based on the metrics.
* **Email Reporting:** The `analyzer-service` compiles AI-generated insights and sends a summary report via email on a schedule (default: weekly).
* **Containerized:** The entire stack (Java apps, Prometheus, Grafana) is configured to run easily using Docker Compose. 

---

## 🛠️ Components

1.  **`performance-demo`:**
    * Spring Boot 3.x application exposing `/api/fast` and `/api/slow/{id}`.
    * Exposes metrics at `/actuator/prometheus` on port `8080`.
    * Built with Java 21, Spring Web, Actuator, Micrometer Prometheus Registry.
2.  **`analyzer-service`:**
    * Spring Boot 3.x application running scheduled tasks.
    * Queries Prometheus (port `9090`) using Spring WebFlux (`WebClient`).
    * Communicates with Groq API (OpenAI compatible) using `openai-java` library.
    * Sends email reports using `spring-boot-starter-mail`.
    * Runs on port `8081`.
    * Built with Java 21, Spring WebFlux, Spring Mail.
3.  **Prometheus:**
    * Official `prom/prometheus` Docker image.
    * Configured via `prometheus.yml` to scrape `performance-demo`.
    * Accessible at `http://localhost:9090`.
4.  **Grafana:**
    * Official `grafana/grafana` Docker image.
    * Can be configured to use Prometheus as a data source.
    * Accessible at `http://localhost:3000` (default login: `admin`/`admin`).

---

## 📋 Prerequisites

* **Docker & Docker Compose:** To build and run the containerized stack. ([Install Docker](https://docs.docker.com/get-docker/))
* **Git:** For cloning the repository.
* **Java 21 & Maven:** (Optional) Needed only if you want to build/run the Spring Boot applications *outside* of Docker.
* **Groq API Key:** A free API key from [GroqCloud](https://groq.com/) is required for the AI analysis.
* **Email Account (Gmail recommended):** An email account configured for SMTP sending is needed for email reports. For Gmail, an **App Password** is required if 2FA is enabled.

---

## 🚀 How to Run (Docker Compose)

This is the recommended way to run the entire system.

1.  **Clone the Repository:**
    ```bash
    git clone [https://github.com/YOUR_USERNAME/ai-api-performance-optimizer.git](https://github.com/YOUR_USERNAME/ai-api-performance-optimizer.git)
    cd ai-api-performance-optimizer
    ```

2.  **Set Environment Variables:**
    The `analyzer-service` requires sensitive information passed via environment variables. Set these in your terminal *before* running Docker Compose:

    * **Linux/macOS:**
        ```bash
        export AI_API_KEY='gsk_YourGroqApiKey'
        export MAIL_USERNAME='your-gmail-username@gmail.com'
        export MAIL_PASSWORD='your-gmail-app-password'
        export MAIL_RECIPIENT='recipient-email@example.com'
        ```
    * **Windows (PowerShell):**
        ```powershell
        $env:AI_API_KEY='gsk_YourGroqApiKey'
        $env:MAIL_USERNAME='your-gmail-username@gmail.com'
        $env:MAIL_PASSWORD='your-gmail-app-password'
        $env:MAIL_RECIPIENT='recipient-email@example.com'
        ```
    * **Windows (CMD):**
        ```cmd
        set AI_API_KEY=gsk_YourGroqApiKey
        set MAIL_USERNAME=your-gmail-username@gmail.com
        set MAIL_PASSWORD=your-gmail-app-password
        set MAIL_RECIPIENT=recipient-email@example.com
        ```
    *(Replace placeholders with your actual Groq key, email credentials, and desired report recipient).*

3.  **Build and Start Services:**
    From the project root directory (`ai-optimizer-project`), run:
    ```bash
    docker-compose up --build -d
    ```
    * `up`: Creates and starts the containers.
    * `--build`: Builds the Docker images for `performance-demo` and `analyzer-service`.
    * `-d`: Runs containers in the background (detached mode).

4.  **Access Services:**
    * **Performance Demo API:** `http://localhost:8080` (e.g., `http://localhost:8080/api/slow/1`)
    * **Prometheus UI:** `http://localhost:9090`
    * **Grafana UI:** `http://localhost:3000` (Login: `admin`/`admin`)

5.  **Generate Test Data:**
    Hit the slow endpoint multiple times to generate metrics for analysis:
    * **Shell/Bash/Git Bash:**
        ```bash
        for i in {1..45}; do curl http://localhost:8080/api/slow/test-$i ; echo "" ; sleep 0.1; done
        ```
    * **PowerShell:**
        ```powershell
        1..45 | ForEach-Object { Invoke-WebRequest -Uri "http://localhost:8080/api/slow/test-$_"; Start-Sleep -Milliseconds 100 }
        ```

6.  **Check Logs:**
    View the logs, especially for the analyzer service:
    ```bash
    docker-compose logs -f analyzer-service
    ```
    You should see it query Prometheus, potentially send prompts to Groq, log AI insights, and eventually attempt to send an email report (based on the schedule in `WeeklyReportTask.java`).

7.  **Stopping:**
    To stop and remove the containers, network, etc.:
    ```bash
    docker-compose down
    ```

---

## ⚙️ Configuration

* **Prometheus:** Edit `prometheus.yml` to change scrape intervals or add new targets.
* **Grafana:** Configure dashboards and data sources via the Grafana UI (`http://localhost:3000`). You can import dashboard ID `12900` for a good Spring Boot 3 overview.
* **Analyzer Service:**
    * AI settings (`ai.*`), Email settings (`spring.mail.*`, `performance.report.*`) are primarily configured via environment variables in `docker-compose.yml`.
    * Prometheus query thresholds (`P95_LATENCY_QUERY`, `ERROR_RATE_QUERY`) are set in `PerformanceAnalyzerTask.java`.
    * Analysis frequency (`@Scheduled(fixedRate=...)` in `PerformanceAnalyzerTask.java`)
    * Report email frequency (`@Scheduled(cron=...)` in `WeeklyReportTask.java`)

---

## 💻 Local Development (Without Docker)

You can run the Spring Boot apps locally using Maven:

1.  **Run `performance-demo`:**
    ```bash
    cd performance-demo
    mvn spring-boot:run
    ```
    (Runs on `http://localhost:8080`)

2.  **Run Prometheus & Grafana:** You can still run these using the Docker commands from earlier steps (making sure Prometheus targets `host.docker.internal:8080` or `localhost:8080` in `prometheus.yml`).

3.  **Run `analyzer-service`:**
    * Set environment variables (or configure secrets directly in `application.properties` - **not recommended for Git**).
    * Ensure `prometheus.server.url=http://localhost:9090` is set in `application.properties`.
    ```bash
    cd ../analyzer-service
    mvn spring-boot:run
    ```
    (Runs on `http://localhost:8081`)

---

## 💡 Potential Improvements

* Store AI insights in a database (e.g., PostgreSQL) for better persistence and querying.
* Refine AI prompts for more specific or actionable suggestions.
* Implement better error handling and resilience (e.g., retries for API calls).
* Use Prometheus Alertmanager for real-time alerting on critical thresholds.
* Enhance email report formatting (HTML).
* Use Docker Secrets or Vault for managing sensitive credentials.
* Add unit and integration tests.

---

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details (You should create a LICENSE file with the MIT license text).
