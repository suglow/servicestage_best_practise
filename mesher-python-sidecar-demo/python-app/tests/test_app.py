import unittest

import requests

from app import create_app


class FakeResponse:
    status_code = 200

    @staticmethod
    def json():
        return {"service": "java-provider", "users": []}


class PythonAppTest(unittest.TestCase):

    def test_health_contract(self):
        client = create_app().test_client()

        response = client.get("/health")

        self.assertEqual(200, response.status_code)
        self.assertEqual(
            {"status": "ok", "service": "python-app", "port": 9000},
            response.get_json(),
        )

    def test_show_calls_java_provider(self):
        calls = []

        def fake_get(url, timeout):
            calls.append((url, timeout))
            return FakeResponse()

        client = create_app(http_get=fake_get).test_client()

        response = client.get("/show")

        self.assertEqual(200, response.status_code)
        self.assertEqual([("http://java-provider/api/users", 10)], calls)
        self.assertEqual(
            200,
            response.get_json()["java_provider_response"]["status"],
        )

    def test_show_returns_demo_error_payload(self):
        def failing_get(_url, timeout):
            self.assertEqual(10, timeout)
            raise requests.exceptions.RequestException("provider unavailable")

        client = create_app(http_get=failing_get).test_client()

        response = client.get("/show")

        self.assertEqual(200, response.status_code)
        self.assertEqual(
            {
                "status": "error",
                "error": "provider unavailable",
            },
            response.get_json()["java_provider_response"],
        )


if __name__ == "__main__":
    unittest.main()
