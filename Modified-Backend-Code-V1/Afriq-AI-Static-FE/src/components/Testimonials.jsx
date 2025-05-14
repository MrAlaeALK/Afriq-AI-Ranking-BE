import React from "react";
import TestimonialCard from "./TestimonialCard";

const Testimonials = () => {
    const testimonials = [
      {
        avatar: "",
        name: "Dr. Amadou Diallo",
        title: "Chercheur, Université de Dakar",
        quote: "\"Afriq'AI offre une vision claire et précise des progrès réalisés en matière d'ouverture des données sur le continent.\"",
        bgColor: "bg-purple-100"
      },
      {
        avatar: "",
        name: "Sarah Okonjo",
        title: "Analyste de données, Open Data Kenya",
        quote: "\"Un outil indispensable pour les décideurs politiques et les acteurs du développement en Afrique.\"",
        bgColor: "bg-green-100"
      },
      {
        avatar: "",
        name: "Jean-Pierre Nkurunziza",
        title: "Ministre du Numérique, Rwanda",
        quote: "\"Grâce à Afriq'AI, nous pouvons identifier les domaines prioritaires pour améliorer notre politique d'ouverture des données.\"",
        bgColor: "bg-blue-100"
      }
    ];
  
    return (
      <section className="py-16 bg-white">
        <div className="container mx-auto px-4">
          <h3 className="text-2xl md:text-3xl font-bold text-center mb-12">Ce qu'ils en disent</h3>
          
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-8">
            {testimonials.map((testimonial, index) => (
              <TestimonialCard 
                key={index}
                avatar={testimonial.avatar}
                name={testimonial.name}
                title={testimonial.title}
                quote={testimonial.quote}
                bgColor={testimonial.bgColor}
              />
            ))}
          </div>
        </div>
      </section>
    );
  };
  
  export default Testimonials;
  