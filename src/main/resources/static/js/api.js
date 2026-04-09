// ===== API CONFIGURATION =====
const API_BASE = 'http://localhost:8081/api';

// ===== API ENDPOINTS =====
const API = {
    cities: {
        getAll: () => fetch(`${API_BASE}/theatres/cities`)
    },
    movies: {
        getAll: () => fetch(`${API_BASE}/movies`),
        getByLanguage: (language) => fetch(`${API_BASE}/movies/language/${language}`),
        getByCity: (city) => fetch(`${API_BASE}/movies/city/${city}`)
    },
    theatres: {
        getAll: () => fetch(`${API_BASE}/theatres`),
        getByLocation: (location) => fetch(`${API_BASE}/theatres/location/${location}`),
        getByCityAndMovie: (city, movieId) => fetch(`${API_BASE}/theatres/city/${city}/movie/${movieId}`)
    },
    shows: {
        getByMovieId: (movieId) => fetch(`${API_BASE}/shows/movie/${movieId}`),
        getByScreenId: (screenId) => fetch(`${API_BASE}/shows/screen/${screenId}`)
    },
    seats: {
        getByScreenAndShow: (screenId, showId, userId) => fetch(`${API_BASE}/seats/screen/${screenId}/show/${showId}?userId=${userId}`),
        lock: (showId, seatId, userId) => fetch(`${API_BASE}/seats/lock`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ showId, seatId, userId })
        }),
        unlock: (showId, seatId, userId) => fetch(`${API_BASE}/seats/lock`, {
            method: 'DELETE',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ showId, seatId, userId })
        })
    },
    bookings: {
        book: (bookingData) => fetch(`${API_BASE}/bookings/book`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(bookingData)
        }),
        getById: (bookingId) => fetch(`${API_BASE}/bookings/${bookingId}`),
        getByUserId: (userId) => fetch(`${API_BASE}/bookings/user/${userId}`),
        getByShowId: (showId) => fetch(`${API_BASE}/bookings/show/${showId}`),
        getAvailableSeats: (showId) => fetch(`${API_BASE}/bookings/show/${showId}/available-seats`),
        cancel: (bookingId) => fetch(`${API_BASE}/bookings/${bookingId}`, {
            method: 'DELETE'
        })
    }
};

// ===== HELPER FUNCTIONS =====

/**
 * Make API call with error handling
 */
async function apiCall(apiFunction, endpointName = 'API') {
    try {
        console.log(`Fetching ${endpointName}...`);
        const response = await apiFunction();
        console.log(`Response status for ${endpointName}:`, response.status);
        if (!response.ok) {
            const errorText = await response.text();
            console.error(`HTTP Error for ${endpointName}:`, response.status, errorText);
            throw new Error(`HTTP ${response.status}: ${response.statusText}`);
        }
        const data = await response.json();
        console.log(`Successfully fetched ${endpointName}:`, data);
        return data;
    } catch (error) {
        console.error(`API Error for ${endpointName}:`, error);
        if (error.name === 'TypeError' && error.message.includes('fetch')) {
            showError(`Network Error: Cannot connect to server. Please ensure the backend is running on http://localhost:8081`);
        } else {
            showError(`Failed to fetch ${endpointName}: ${error.message}`);
        }
        return null;
    }
}

/**
 * Fetch cities from API
 */
async function fetchCities() {
    return await apiCall(() => API.cities.getAll(), 'Cities');
}

/**
 * Fetch movies from API
 */
async function fetchMovies() {
    return await apiCall(() => API.movies.getAll(), 'Movies');
}

/**
 * Fetch movies by city
 */
async function fetchMoviesByCity(city) {
    return await apiCall(() => API.movies.getByCity(city), `Movies in ${city}`);
}

/**
 * Fetch theatres from API
 */
async function fetchTheatres() {
    return await apiCall(() => API.theatres.getAll(), 'Theatres');
}

/**
 * Fetch theatres by city and movie
 */
async function fetchTheatresByCityAndMovie(city, movieId) {
    return await apiCall(() => API.theatres.getByCityAndMovie(city, movieId), `Theatres in ${city} for movie ${movieId}`);
}

/**
 * Fetch shows for a movie
 */
async function fetchShowsByMovieId(movieId) {
    return await apiCall(() => API.shows.getByMovieId(movieId), `Shows for movie ${movieId}`);
}

/**
 * Fetch shows for a screen/theatre
 */
async function fetchShowsByScreenId(screenId) {
    return await apiCall(() => API.shows.getByScreenId(screenId));
}

async function fetchSeatsByScreenAndShow(screenId, showId, userId) {
    return await apiCall(() => API.seats.getByScreenAndShow(screenId, showId, userId), `Seats for screen ${screenId}`);
}

async function lockSeat(showId, seatId, userId) {
    try {
        const response = await API.seats.lock(showId, seatId, userId);
        return response.ok;
    } catch (e) { return false; }
}

async function unlockSeat(showId, seatId, userId) {
    try {
        await API.seats.unlock(showId, seatId, userId);
    } catch (e) { /* silent */ }
}

/**
 * Get user's bookings
 */
async function fetchUserBookings(userId) {
    return await apiCall(() => API.bookings.getByUserId(userId));
}

/**
 * Get available seats for a show
 */
async function fetchAvailableSeats(showId) {
    return await apiCall(() => API.bookings.getAvailableSeats(showId));
}

/**
 * Get bookings for a show
 */
async function fetchShowBookings(showId) {
    return await apiCall(() => API.bookings.getByShowId(showId));
}

/**
 * Create booking
 */
async function createBooking(userId, showId, seatIds) {
    return await apiCall(() => API.bookings.book({
        userId: parseInt(userId),
        showId: parseInt(showId),
        seatIds: seatIds.map(id => parseInt(id))
    }), 'Create Booking');
}

/**
 * Cancel booking
 */
async function cancelBooking(bookingId) {
    showLoading(true);
    try {
        const response = await API.bookings.cancel(bookingId);
        if (!response.ok) {
            throw new Error(`HTTP Error: ${response.status}`);
        }
        return await response.json();
    } catch (error) {
        console.error('Cancel Error:', error);
        showError(`Failed to cancel booking: ${error.message}`);
        return null;
    } finally {
        showLoading(false);
    }
}
