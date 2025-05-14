// components/PageTitle.js
import React from 'react';

function PageTitle() {
  return (
    <main className="flex-grow pt-20 pb-16">
      <section className="bg-purple-700 py-12 mb-8">
        <div className="container mx-auto px-4">
          <h1 className="text-3xl md:text-4xl font-bold text-white mb-4">Carte interactive de l'Afrique</h1>
         <p className="text-lg text-purple-100">Explorez et comparez les performances des pays africains selon différents critères.</p>
        </div>
      </section>
    </main>
  );
}

export default PageTitle;