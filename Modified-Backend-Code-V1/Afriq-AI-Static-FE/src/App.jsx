// App.jsx
import React from 'react';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import Header from './components/Header';
import HomePage from './components/pages/HomePage';
import ComparisonPage from './components/pages/ComparisonPage';
import MapPage from './components/pages/MapPage';
import RankingPage from './components/pages/RankingPage';
import Footer from './components/Footer';
import './App.css';

function App() {
  return (
    <Router>
      <div className="font-sans">
        <Header />
        <Routes>
          <Route path="/" element={<HomePage />} />
          <Route path="/carte" element={<MapPage />} />
          <Route path="/classement" element={<RankingPage />} />
          <Route path="/comparer" element={<ComparisonPage/>} />
        </Routes>
        <Footer />
      </div>
    </Router>
  );
}

export default App;