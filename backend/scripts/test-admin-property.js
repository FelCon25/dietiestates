const axios = require('axios');

const BASE_URL = 'http://localhost:3000';

async function testAdminPropertyCreation() {
    console.log('🧪 Testing Admin Agency Property Creation...\n');

    // Step 1: Register an admin agency user
    console.log('1️⃣ Registering admin agency user:');
    let adminToken;
    try {
        const registerResponse = await axios.post(`${BASE_URL}/auth/register`, {
            email: 'admin@agency.com',
            password: 'password123',
            firstName: 'Admin',
            lastName: 'Agency',
            role: 'ADMIN_AGENCY'
        });
        console.log('✅ Admin agency registration successful');
        adminToken = registerResponse.data.accessToken;
    } catch (error) {
        console.log('❌ Admin agency registration failed:', error.response?.data || error.message);
        return;
    }

    // Step 2: Create a property with images
    console.log('\n2️⃣ Creating property with images:');
    try {
        const FormData = require('form-data');
        const fs = require('fs');
        const form = new FormData();
        
        // Add property data
        form.append('title', 'Villa Admin Test');
        form.append('description', 'Una villa creata da admin agency');
        form.append('price', '750000');
        form.append('surfaceArea', '250');
        form.append('rooms', '6');
        form.append('floors', '3');
        form.append('elevator', 'true');
        form.append('energyClass', 'A+');
        form.append('concierge', 'true');
        form.append('airConditioning', 'true');
        form.append('furnished', 'true');
        form.append('type', 'SALE');
        form.append('address', 'Via Admin 456');
        form.append('city', 'Roma');
        form.append('postalCode', '00100');
        form.append('province', 'RM');
        form.append('country', 'Italia');
        form.append('latitude', '41.9028');
        form.append('longitude', '12.4964');
        form.append('propertyCondition', 'NEW');

        // Add a test image (create a simple text file as image for testing)
        const testImagePath = './test-image.txt';
        fs.writeFileSync(testImagePath, 'This is a test image content');
        form.append('images', fs.createReadStream(testImagePath));

        const propertyResponse = await axios.post(`${BASE_URL}/property`, form, {
            headers: {
                'Authorization': `Bearer ${adminToken}`,
                ...form.getHeaders()
            }
        });
        
        console.log('✅ Property creation successful:', propertyResponse.data.propertyId);
        
        // Clean up test file
        fs.unlinkSync(testImagePath);
        
    } catch (error) {
        console.log('❌ Property creation failed:', error.response?.data || error.message);
    }

    console.log('\n🎉 Admin agency property creation test completed!');
}

// Run the test
testAdminPropertyCreation().catch(console.error);
