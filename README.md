# Walmart API Documentation

This document provides information about all API routes implemented in the Walmart microservices project.

## Table of Contents

- [API Gateway](#api-gateway)
- [User Service](#user-service)
- [Transaction Service](#transaction-service)
- [Search Service](#search-service)
- [Admin Service](#admin-service)

## API Gateway

The API Gateway routes requests to the appropriate microservices.

| Base Path | Target Service |
|-----------|----------------|
| `/api/users/**` | User Service |
| `/api/search/**` | Search Service |
| `/api/orders/**` | Transaction Service |
| `/api/admins/**` | Admin Service |
| `/api/products/**` | Admin Service |
| `/api/promotions/**` | Admin Service |

## User Service

Base URL: `/api/users`

| Method | Route | Description | Inputs | Outputs |
|--------|-------|-------------|--------|---------|
| GET | `/test` | Test endpoint | None | Success message |
| POST | `/` | Create a new user | `UserModel` object with email and password | Created user object or error message |
| GET | `/{id}` | Get user by ID | Path parameter: `id` | User object |
| PUT | `/{id}` | Update user | Path parameter: `id`, Request body: `UserModel` | Updated user object |
| DELETE | `/{id}` | Delete user | Path parameter: `id` | No content response |
| POST | `/login` | User login | Request body: `{email, password}` | JWT token and user details or error message |
| GET | `/verify` | Email verification | Query parameter: `token` | Success or error message |

## Transaction Service

Base URL: `/api/transactions`

| Method | Route | Description | Inputs | Outputs |
|--------|-------|-------------|--------|---------|
| POST | `/` | Create a transaction | Request body: `Transaction`, Optional query parameter: `promoCode` | Created transaction or error message |
| GET | `/{id}` | Get transaction by ID | Path parameter: `id` | Transaction object or error message |
| GET | `/` | List all transactions | None | List of transactions or error message |
| PUT | `/{id}` | Update transaction | Path parameter: `id`, Request body: `Transaction` | Updated transaction or error message |
| DELETE | `/{id}` | Delete transaction | Path parameter: `id` | No content response or error message |
| GET | `/user/{userId}` | Get user's transactions | Path parameter: `userId` | List of transactions or error message |
| GET | `/order/{orderId}` | Get order transactions | Path parameter: `orderId` | List of transactions or error message |
| GET | `/user/{userId}/history` | Get user's order history | Path parameter: `userId` | Order history or error message |

## Search Service

Base URL: `/api/search`

| Method | Route | Description | Inputs | Outputs |
|--------|-------|-------------|--------|---------|
| GET | `/products` | Get all products | None | List of all products |
| GET | `/products/top-three-lowest-price` | Get products with lowest prices | None | List of the top 3 lowest priced products |
| GET | `/products/search` | Search products | Query parameter: `keyword` (optional) | List of matching products |
| GET | `/products/low-stock` | Get low stock products | None | List of products with low stock |
| GET | `/products/filter/category` | Filter by category | Query parameter: `category` (optional) | List of products in the specified category |
| GET | `/products/filter/price` | Filter by price range | Query parameters: `min` and `max` (both optional) | List of products within the price range |

## Admin Service

### Admin Endpoints

Base URL: `/api/admins`

| Method | Route | Description | Inputs | Outputs |
|--------|-------|-------------|--------|---------|
| POST | `/create` | Create admin | Request body: `Admin` object | Created admin or error message |
| POST | `/login` | Admin login | Query parameters: `username` and `password` | JWT token and username or error message |
| GET | `/` | Get all admins | None | List of all admins or error message |
| GET | `/{id}` | Get admin by ID | Path parameter: `id` | Admin object or error message |

### Product Endpoints

Base URL: `/api/products`

| Method | Route | Description | Inputs | Outputs |
|--------|-------|-------------|--------|---------|
| GET | `/` | Get all products | None | List of all products |
| GET | `/{id}` | Get product by ID | Path parameter: `id` | Product object or not found response |
| POST | `/` | Create a product | Request body: `Product` object | Created product |
| PUT | `/{id}` | Update product | Path parameter: `id`, Request body: `ProductUpdateDTO` | Updated product or not found response |

### Promotion Endpoints

Base URL: `/api/promotions`

| Method | Route | Description | Inputs | Outputs |
|--------|-------|-------------|--------|---------|
| POST | `/` | Create promotion | Request body: `Promotion` object | Created promotion or error message |
| GET | `/` | Get all promotions | None | List of all promotions |
| GET | `/{id}` | Get promotion by ID | Path parameter: `id` | Promotion object |
| PUT | `/{id}` | Update promotion | Path parameter: `id`, Request body: `Promotion` object | Updated promotion or not found response |
| DELETE | `/{id}` | Delete promotion | Path parameter: `id` | Success or error message | 
