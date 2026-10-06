# AURA — AI-Powered Financial Intelligence Platform

**Your Personal AI Financial Advisor for India**

_Powered by Azure OpenAI GPT-4.1 & a Multi‑Agent Architecture, with real‑time data via the Fi.Money MCP_

[![Azure](https://img.shields.io/badge/Azure-Deployed-0089D6?style=for-the-badge&logo=microsoftazure)](https://azure.microsoft.com)
[![Java](https://img.shields.io/badge/Java-21-007396?style=for-the-badge&logo=java)](https://openjdk.org/)
[![MongoDB](https://img.shields.io/badge/MongoDB-Atlas-47A248?style=for-the-badge&logo=mongodb)](https://mongodb.com/)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg?style=for-the-badge)](LICENSE)

[![GitHub](https://img.shields.io/badge/GitHub-paridhi%20raghuvanshi-181717?style=for-the-badge&logo=github)](https://github.com/paridhi-raghuvanshi/AI-Powered-Financial-Intelligence-.git)
[![Live Demo](https://img.shields.io/badge/Live-aura--finance--ai.azurewebsites.net-00D4FF?style=for-the-badge)](https://aura-finance-ai.azurewebsites.net)

---

## Video Walkthrough

See AURA in action!

https://github.com/user-attachments/assets/9b51bcbc-746c-40b7-bed6-de4b6bc509ea

> **Note:** GitHub‑hosted videos are muted by default due to browser restrictions — please unmute to hear the audio.

<p align="center">
  Prefer YouTube? <a href="https://youtu.be/3q4uTliRNr8"><strong>Watch it here</strong></a>
</p>

---

## Table of Contents

- Overview
- Features
- Architecture
- AI Agents
- Quick Start
- Deployment
- API Reference
- Project Structure
- Configuration
- Security
- Contributing
- License
- Acknowledgments

---

## Overview

**AURA** is an advanced AI‑powered financial intelligence platform that combines five specialized AI agents with real‑time financial data from the Fi.Money MCP to deliver personalized financial strategies, quantitative analysis, and actionable insights for Indian investors.

### Key Highlights

- Multi‑Agent AI – five specialized financial AI agents working in orchestration
- Real‑Time Data – live financial data via Fi.Money MCP integration
- MongoDB Atlas – persistent storage for users, chat history, and analyses
- Azure Deployment – deployed on Microsoft Azure App Service
- Modern UI – responsive design with glass‑morphism aesthetics
- Real‑Time Chat – interactive financial consultation with live progress tracking
- Secure Auth – Google OAuth + email‑based authentication

---

## Features

### Core Capabilities

| Feature | Description | Status |
|---|---|---|
| Multi‑Agent AI | 5 specialized agents for comprehensive analysis | ✅ |
| Fi.Money MCP | Real‑time financial data integration | ✅ |
| RAG Knowledge Base | Financial knowledge retrieval | ✅ |
| Portfolio Analytics | Net worth, investments, transactions | ✅ |
| Credit Reports | Credit score and history | ✅ |
| Chat History | Persistent, user‑isolated conversations | ✅ |
| Demo Accounts | 3 pre‑configured demo profiles | ✅ |
| Google OAuth | Secure authentication | ✅ |

### Dashboard Features

- Net Worth Tracking – real‑time portfolio valuation
- Investment Analysis – mutual funds & stocks breakdown
- Credit Score – credit report integration
- Transaction History – recent financial activity
- Portfolio Chart – visual asset allocation
- Profile Shuffle – switch between demo MCP profiles

---

## Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                       AURA Platform                          │
├─────────────────────────────────────────────────────────────┤
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐          │
│  │   Chat UI   │  │  Dashboard  │  │ Onboarding  │          │
│  └──────┬──────┘  └──────┬──────┘  └──────┬──────┘          │
│         │                │                 │                 │
│  ┌──────┴───────────────────────────────────────┴──────┐          │
│  │              Spring Boot (REST) + WebSocket            │          │
│  └──────┬────────────────┬─────────────────┬──────┘          │
│         │                │                 │                 │
│  ┌──────┴──────┐  ┌──────┴──────┐  ┌──────┴──────┐          │
│  │    Agent    │  │   MongoDB   │  │  Fi.Money   │          │
│  │Orchestrator │  │    Atlas    │  │     MCP     │          │
│  └──────┬──────┘  └─────────────┘  └─────────────┘          │
│         │                                                     │
│  ┌──────┴─────────────────────────────────────────┐          │
│  │            5 AI Agents (Azure OpenAI)           │          │
│  │  ┌──────────┬────────┬────────┬─────────┐       │          │
│  │  │Strategist│ Quant  │  Doer  │ Realist │       │          │
│  │  └──────────┴────────┴────────┴─────────┘       │          │
│  │               ┌─────────────┐                    │          │
│  │               │Communicator │                    │          │
│  │               └─────────────┘                    │          │
│  └──────────────────────────────────────────────────┘          │
└─────────────────────────────────────────────────────────────┘
```

---

## AI Agents

AURA's intelligence comes from **5 specialized AI agents** powered by Azure OpenAI GPT‑4.1:

| Agent | Role | Capabilities |
|---|---|---|
| **Strategist** | Financial Planning | Goal planning, risk assessment, asset allocation |
| **Quant** | Quantitative Analysis | XIRR/CAGR calculations, volatility analysis |
| **Doer** | Implementation | Action plans, platform guidance, timelines |
| **Realist** | Market Intelligence | Data validation, market insights |
| **Communicator** | User Engagement | Personalized responses, progress updates |

---

## Quick Start

### Prerequisites

- Java 21+
- Maven 3.9+
- MongoDB (Atlas or local)
- Azure OpenAI API access

### Local Development

```bash
# Clone the repository
git clone https://github.com/paridhi-raghuvanshi/AI-Powered-Financial-Intelligence-.git
cd AI-Powered-Financial-Intelligence-

# Build the Java backend (Spring Boot)
cd aura-platform-java
./mvnw clean package -DskipTests
# Run the backend (default port 3000)
java -jar target/*.jar &

# Set up front‑end environment variables
cd ../aura-platform
cp env.sample .env
# Edit .env with your API keys

# Start the front‑end dev server
npm install
npm run dev   # runs Vite on http://localhost:5173
```

### Environment Variables

Create a `.env` file inside `aura-platform/`:

```env
# Azure OpenAI
AZURE_OPENAI_ENDPOINT=https://your-resource.openai.azure.com/
AZURE_OPENAI_API_KEY=your-api-key
AZURE_OPENAI_CHATGPT_DEPLOYMENT=gpt-4.1
AZURE_OPENAI_API_VERSION=2025-01-01-preview

# MongoDB
MONGODB_URI=mongodb+srv://user:pass@cluster.mongodb.net/aura_finance

# Fi.Money MCP
FI_MCP_URL=https://your-mcp-server.azurewebsites.net

# Google OAuth (optional)
GOOGLE_CLIENT_ID=your-client-id
GOOGLE_CLIENT_SECRET=your-client-secret

# Application
NODE_ENV=development
PORT=3000
```

---

## Deployment

### Azure App Service

```bash
az login
az group create --name aura-rg --location centralindia
az appservice plan create --name aura-plan --resource-group aura-rg --sku B1 --is-linux
az webapp create --resource-group aura-rg --plan aura-plan --name aura-finance --runtime "JAVA:21"
az webapp config appsettings set --resource-group aura-rg --name aura-finance --settings \
  AZURE_OPENAI_ENDPOINT="your‑endpoint" \
  AZURE_OPENAI_API_KEY="your‑key" \
  MONGODB_URI="your‑mongodb‑uri" \
  FI_MCP_URL="your‑mcp‑url"
az webapp deployment source config --name aura-finance --resource-group aura-rg \
  --repo-url https://github.com/paridhi-raghuvanshi/AI-Powered-Financial-Intelligence-.git \
  --branch main --manual-integration
```

---

## API Reference

### Chat API

```http
POST /api/chat
Content-Type: application/json

{
  "message": "I'm 25, earning ₹80,000/month. How should I invest?",
  "sessionId": "user-session-123",
  "userId": "user@email.com"
}
```

Response example:

```json
{
  "success": true,
  "response": "Based on your profile...",
  "agentsUsed": ["Strategist", "Quant", "Doer", "Realist", "Communicator"],
  "complexity": "complex",
  "intent": "portfolio_planning",
  "executionTime": "12.5s"
}
```

---

## Project Structure

```text
AURA‑THE‑FINANCE‑AI/
├── aura-platform-java/               # Spring Boot backend
│   ├── src/main/java/...            # Java source files
│   └── pom.xml
├── aura-platform/                    # Front‑end (Vite)
│   ├── public/ ...
│   └── src/ ...
├── fi-mcp-dev/                       # Fi.Money MCP server (Node.js)
│   └── ...
└── README.md
```

---

## Configuration

### Tech Stack

| Component | Technology |
|---|---|
| AI Model | Azure OpenAI GPT‑4.1 |
| Backend | Java 21 + Spring Boot |
| Real‑time | WebSocket |
| Database | MongoDB Atlas |
| Financial Data | Fi.Money MCP |
| Authentication | Google OAuth + Email |
| Deployment | Azure App Service |

---

## Security Features

- Rate limiting (100 requests / 15 min)
- CORS protection
- Helmet security headers
- User‑isolated chat history
- Secure API key handling

---

## Contributing

1. Fork the project
2. Create a feature branch (`git checkout -b feature/awesome-feature`)
3. Commit your changes (`git commit -m "Add awesome feature"`)
4. Push to the branch (`git push origin feature/awesome-feature`)
5. Open a Pull Request

---

## License

This project is licensed under the **MIT License** — see the [LICENSE](LICENSE) file for details.

---

## Acknowledgments

- Microsoft Azure for cloud infrastructure
- OpenAI for the GPT‑4.1 language model
- Fi.Money for MCP financial data integration
- MongoDB for database services

---

BUILT BY TEAM DAWN
