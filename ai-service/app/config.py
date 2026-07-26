from pydantic_settings import BaseSettings, SettingsConfigDict


class Settings(BaseSettings):
    database_url: str
    groq_api_key: str | None = None
    groq_model: str = "openai/gpt-oss-120b"
    upload_dir: str = "./uploads"
    reports_dir: str = "./reports"

    model_config = SettingsConfigDict(
        env_file=".env",
        extra="ignore",
    )


settings = Settings()