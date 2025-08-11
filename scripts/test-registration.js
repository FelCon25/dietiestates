const axios = require('axios');

const BASE_URL = 'http://localhost:3000';

async function testRegistration() {
    console.log('🧪 Testing Registration with Limited Roles...\n');

    // Test 1: Register with USER role (default)
    console.log('1️⃣ Testing USER registration (default role):');
    try {
        const userResponse = await axios.post(`${BASE_URL}/auth/register`, {
            email: 'user@test.com',
            password: 'password123',
            firstName: 'John',
            lastName: 'User'
        });
        console.log('✅ USER registration successful:', userResponse.data.user.role);
    } catch (error) {
        console.log('❌ USER registration failed:', error.response?.data || error.message);
    }

    // Test 2: Register with ADMIN_AGENCY role
    console.log('\n2️⃣ Testing ADMIN_AGENCY registration:');
    try {
        const adminResponse = await axios.post(`${BASE_URL}/auth/register`, {
            email: 'admin@test.com',
            password: 'password123',
            firstName: 'Admin',
            lastName: 'User',
            role: 'ADMIN_AGENCY'
        });
        console.log('✅ ADMIN_AGENCY registration successful:', adminResponse.data.user.role);
    } catch (error) {
        console.log('❌ ADMIN_AGENCY registration failed:', error.response?.data || error.message);
    }

    // Test 3: Try to register with invalid role (should fail)
    console.log('\n3️⃣ Testing invalid role registration (should fail):');
    try {
        const invalidResponse = await axios.post(`${BASE_URL}/auth/register`, {
            email: 'invalid@test.com',
            password: 'password123',
            firstName: 'Invalid',
            lastName: 'User',
            role: 'AGENT' // This should fail
        });
        console.log('❌ Invalid role registration should have failed but succeeded');
    } catch (error) {
        console.log('✅ Invalid role registration correctly failed:', error.response?.data?.message || error.message);
    }

    // Test 4: Try to register with ASSISTANT role (should fail)
    console.log('\n4️⃣ Testing ASSISTANT role registration (should fail):');
    try {
        const assistantResponse = await axios.post(`${BASE_URL}/auth/register`, {
            email: 'assistant@test.com',
            password: 'password123',
            firstName: 'Assistant',
            lastName: 'User',
            role: 'ASSISTANT' // This should fail
        });
        console.log('❌ ASSISTANT role registration should have failed but succeeded');
    } catch (error) {
        console.log('✅ ASSISTANT role registration correctly failed:', error.response?.data?.message || error.message);
    }

    console.log('\n🎉 Registration tests completed!');
}

// Run the test
testRegistration().catch(console.error);
