// Import the functions you need from the SDKs you need
import { initializeApp } from "firebase/app";
import { getAuth } from 'firebase/auth';
import { getFirestore } from 'firebase/firestore';

// https://firebase.google.com/docs/web/setup#available-libraries


const firebaseConfig = {
  apiKey: "AIzaSyDBZhvn7XSr9598EFz8_247xG3ZVkqublo",
  authDomain: "signalement-cloud-s5.firebaseapp.com",
  projectId: "signalement-cloud-s5",
  storageBucket: "signalement-cloud-s5.firebasestorage.app",
  messagingSenderId: "53253013060",
  appId: "1:53253013060:web:5fdf3ef2e7ff69bbdf0611"
};


// Initialize Firebase
const app = initializeApp(firebaseConfig);

const auth = getAuth(app);
const db = getFirestore(app);

export { auth, db};