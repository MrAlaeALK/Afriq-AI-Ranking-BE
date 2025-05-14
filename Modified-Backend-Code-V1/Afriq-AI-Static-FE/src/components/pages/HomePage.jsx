// pages/HomePage.jsx
import React from 'react';
import Hero from '../Hero';
import Criteria from '../Criteria';
import Partners from '../Partners';
import Testimonials from '../Testimonials';
import CallToAction from '../CallToAction';

function HomePage() {
  return (
    <>
      <Hero />
      <Criteria />
      <Partners />
      <Testimonials />
      <CallToAction />
    </>
  );
}

export default HomePage;