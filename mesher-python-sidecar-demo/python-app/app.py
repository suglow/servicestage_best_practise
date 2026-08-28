"""Flask demo application connected to ServiceComb through Mesher."""

import os
from datetime import datetime

import requests
from flask import Flask, jsonify


def create_app(http_get=None):
    """Create the Flask application with an injectable HTTP client for tests."""
    application = Flask(__name__)
    request_get = http_get or requests.get
    java_provider = os.environ.get("JAVA_PROVIDER_SERVICE", "java-provider")

    @application.get("/health")
    def health():
        return jsonify({"status": "ok", "service": "python-app", "port": 9000})

    @application.get("/show")
    def show():
        result = {
            "service": "python-app",
            "port": 9000,
            "timestamp": datetime.now().isoformat(),
        }
        try:
            response = request_get(f"http://{java_provider}/api/users", timeout=10)
            result["java_provider_response"] = {
                "status": response.status_code,
                "body": response.json(),
            }
        except requests.exceptions.RequestException as exception:
            result["java_provider_response"] = {
                "status": "error",
                "error": str(exception),
            }

        return jsonify(result)

    return application


app = create_app()


if __name__ == "__main__":
    app.run(host="0.0.0.0", port=int(os.environ.get("PORT", "9000")))
