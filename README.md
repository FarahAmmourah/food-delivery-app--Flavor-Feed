# 🍔 FlavorFeed – AI-Powered Food Delivery Platform

FlavorFeed is an AI-powered Android food delivery application that combines short-form food reels with real-time food ordering.

Instead of browsing traditional menus, users discover meals through engaging video content, interact with restaurants, and place orders directly from the reels.

This project was developed as a Graduation Project at Yarmouk University.

---

# ✨ Features

- 🎥 Food Reels Feed (TikTok-style experience)
- 🍽 Restaurant Menu Management
- 🛒 Shopping Cart & Checkout
- 🔐 Firebase Authentication
- ☁ Firebase Firestore & Storage
- 📍 GPS Location Selection
- 🔔 Push Notifications
- ❤️ Likes, Comments & Ratings
- 🤖 AI Chatbot Assistant
- 🧠 On-device Sentiment Analysis using TensorFlow Lite
- 🎁 Rewards & Loyalty System
- 💳 Cash & Visa Payment Support

---

# 🧠 AI Features

## Sentiment Analysis

FlavorFeed analyzes customer comments using a TensorFlow Lite model.

The application:

- Cleans and preprocesses comments
- Converts text using Bag-of-Words vectorization
- Runs inference using TensorFlow Lite
- Classifies comments into:
  - Positive
  - Neutral
  - Negative

The final sentiment summary helps restaurants better understand customer satisfaction.

---

## AI Chatbot

The chatbot helps users:

- Discover nearby restaurants
- Recommend meals
- Answer ordering questions
- Improve the ordering experience

---

# 🏗 Architecture

```
Android App (Java + XML)
        │
        ▼
Firebase Authentication
        │
Firebase Firestore
        │
Firebase Storage
        │
Python Django Backend
        │
TensorFlow Lite
```

---

# 🛠 Technology Stack

### Mobile

- Java
- XML
- Android Studio

### Backend

- Python
- Django
- REST APIs

### Database

- Firebase Firestore
- Firebase Storage

### AI

- TensorFlow Lite
- Bag-of-Words
- Sentiment Analysis

### Services

- Firebase Authentication
- Push Notifications
- GPS Location Services

---

# 📱 Main Modules

### Customer

- Register/Login
- Browse Food Reels
- Search Restaurants
- View Menus
- Add to Cart
- Checkout
- Track Orders
- Rewards
- Ratings

### Restaurant

- Upload Food Reels
- Manage Menu
- Accept / Reject Orders
- View Ratings
- Manage Profile

---

# 📸 Screenshots

| Splash | Login |
|--------|-------|
| Add Screenshot | Add Screenshot |

| Reels Feed | Restaurant Menu |
|-------------|-----------------|
| Add Screenshot | Add Screenshot |

| Checkout | Rewards |
|----------|---------|
| Add Screenshot | Add Screenshot |

| Sentiment Analysis | Chatbot |
|--------------------|---------|
| Add Screenshot | Add Screenshot |

---

# 🚀 Future Improvements

- Recommendation Engine
- Multi-language Sentiment Analysis
- Better AI Model
- Personalized Food Recommendations
- Social Features Expansion

---

# 👨‍💻 Team

- Farah Ammourah
- Sujoud Nassar
- Aya Habboush

Supervisor

Dr. Ra'ed M Al-khatib

---

# 📄 License

This project was developed for educational purposes as a Graduation Project at Yarmouk University.
