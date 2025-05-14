import React, { useState } from "react";
import PartnerLogo from "./PartnerLogo";

const Partners = () => {

    const [expanded, setExpanded] = useState(false);
    const toggleText = () => {
      setExpanded(!expanded);
    };

    const partners = [
      { name: "Open Data Watch", longName: "Open Data Watch" },
      { name: "UA", longName: "Union Africaine" },
      { name: "BM", longName: "Banque Mondiale" },
      { name: "BAD", longName: "Banque Africaine de Développement" }
    ];
  
    return (
      <section className="py-16 bg-gray-50">
        <div className="container mx-auto px-4">
          <h3 className="text-2xl md:text-3xl font-bold text-center mb-12">Nos sources de données</h3>
          
          <div className="flex flex-wrap justify-center items-center gap-8 md:gap-16">
            {partners.map((partner, index) => (
              <PartnerLogo key={index} name={partner.name} longName={partner.longName} />
            ))}
          </div>
          
          {expanded && (
          <>
            <br />
            <div className="flex flex-wrap justify-center items-center gap-8 md:gap-16">
              {partners.map((partner, index) => (
                <PartnerLogo key={index} name={partner.name} longName={partner.longName} />
              ))}
            </div>
          </>
          )}

          <div className="text-center mt-6">
            <button
              onClick={toggleText}
              className="px-6 py-3 bg-purple-500 text-white font-medium rounded-lg hover:bg-purple-600 transition-colors shadow-md"
            >
              {expanded ? "Voir moins" : "Voir plus"}
            </button>
          </div>

        </div>
      </section>
    );
  };
  export default Partners;