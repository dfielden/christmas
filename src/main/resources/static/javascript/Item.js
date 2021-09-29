export class Item {
    product;
    price;
    location;
    url;
    additionalInfo;

    constructor(product, price, location, url, additionalInfo) {
        this.product = product;
        this.price = price;
        this.location = location;
        this.url = url;
        this.additionalInfo = additionalInfo;
    }
}
