# Catchy

## Introduction

This repository contains the source code for the "Catchy" project. The project utilizes Firebase for backend services, including Firestore and Firebase Functions. This guide will help you set up Firebase and deploy cloud functions.

## Prerequisites

- Node.js and npm installed
- Firebase CLI installed
- Firebase project created

## Getting Started

### 1. Clone the Repository

```bash
git clone https://github.com/MarsadMaqsood/catchy.git
cd catchy
```

### 2. Install Dependencies

Navigate to the `Cloud Functions` directory and install the necessary dependencies:

```bash
cd "Cloud Functions"
npm install
```

### 3. Firebase Setup

#### 3.1. Initialize Firebase

If you haven't initialized Firebase in your project directory, do so now:
```bash
firebase init
```

Choose the following options during initialization:

- Firestore
- Functions

#### 3.2. Set Up Firebase Configuration

Update your Firebase configuration in the firebase.json and .firebaserc files with your project's details.


### 4. Deploy Cloud Functions

Deploy your functions to Firebase:
```bash
firebase deploy --only functions
```

## Project Structure
- **app/:** Contains the main application source code.
- **Cloud Functions/:** Contains Firebase functions.

## Additional Resources
- [Youtube - Coding With Marsad](https://youtube.com/playlist?list=PLFzlb57tNKUOrFIcicZ88qmaeHTA_9t_6&si=ULZvmfuD_2VKngFF)
- [Firebase Documentation](https://firebase.google.com/docs/android/setup)
- [Node.js](https://nodejs.org/en)
- [Firebase CLI](https://firebase.google.com/docs/cli)


## License
This project is licensed under the Apache-2.0 License - see the [LICENSE](https://github.com/MarsadMaqsood/Catchy/blob/master/LICENSE) file for details.

## Author
[Marsad Maqsood](https://github.com/MarsadMaqsood)

Feel free to open an issue or submit a pull request for any issues or contributions.
