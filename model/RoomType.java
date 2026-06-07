package model;

/**
 * Enum representing the four room categories available at Grand Palace Hotel.
 * Each type carries a nightly rate, display name, and amenities description.
 */
public enum RoomType {

    STANDARD(2500,  "Standard Room",    "Wi-Fi, TV, Air-conditioning, En-suite Bathroom"),
    DELUXE  (4500,  "Deluxe Room",      "Wi-Fi, Smart TV, Mini-bar, City View, Air-conditioning"),
    SUITE   (8500,  "Suite",            "Wi-Fi, Smart TV, Jacuzzi, Living Area, Kitchenette, Sea View"),
    PRESIDENTIAL(18000, "Presidential Suite",
                                        "Wi-Fi, Smart TV, Private Pool, Butler Service, Panoramic View, Full Kitchen");

    private final double pricePerNight;
    private final String displayName;
    private final String amenities;

    RoomType(double pricePerNight, String displayName, String amenities) {
        this.pricePerNight = pricePerNight;
        this.displayName   = displayName;
        this.amenities     = amenities;
    }

    public double getPricePerNight() { return pricePerNight; }
    public String getDisplayName()   { return displayName;   }
    public String getAmenities()     { return amenities;     }
}
