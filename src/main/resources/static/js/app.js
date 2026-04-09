// ===== STATE MANAGEMENT =====
const USERS = {
    user1: { id: 1, password: '123', displayName: 'User 1' },
    user2: { id: 2, password: '456', displayName: 'User 2' }
};

let currentUser = null; // { id, username, displayName }

const state = {
    selectedCity: null,
    selectedMovie: null,
    selectedTheatre: null,
    selectedShow: null,
    selectedSeats: [],
    cities: [],
    movies: [],
    theatres: [],
    shows: [],
    bookedSeats: [],
    screenSeats: [],
    lockExpiryTime: null,
    lockTimerInterval: null,
    seatRefreshInterval: null
};

// ===== INITIALIZATION =====
document.addEventListener('DOMContentLoaded', () => {
    initializeEventListeners();
    showSection('login');
    setupNavigation();
    initializeCarousel();
});

// ===== LOGIN =====
function login() {
    const username = document.getElementById('loginUsername').value.trim().toLowerCase();
    const password = document.getElementById('loginPassword').value.trim();
    const errorEl = document.getElementById('loginError');

    const user = USERS[username];
    if (!user || user.password !== password) {
        errorEl.textContent = 'Invalid username or password';
        return;
    }

    errorEl.textContent = '';
    currentUser = { id: user.id, username, displayName: user.displayName };
    document.getElementById('navUsername').textContent = `👤 ${user.displayName}`;
    showSection('cities');
}

function logout() {
    unlockAllSelectedSeats();
    clearLockTimer();
    currentUser = null;
    state.selectedSeats = [];
    document.getElementById('navUsername').textContent = '';
    document.getElementById('loginUsername').value = '';
    document.getElementById('loginPassword').value = '';
    showSection('login');
}

/**
 * Initialize event listeners
 */
function initializeEventListeners() {
    document.querySelectorAll('.nav-link').forEach(link => {
        link.addEventListener('click', (e) => {
            e.preventDefault();
            if (!currentUser) return;
            const section = link.dataset.section;
            showSection(section);
            updateActiveNav(link);
        });
    });
}

/**
 * Setup navigation
 */
function setupNavigation() {
    document.querySelectorAll('.nav-link').forEach(link => {
        link.addEventListener('click', () => {
            if (link.dataset.section === 'bookings') {
                loadUserBookings();
            }
        });
    });
}

/**
 * Update active nav link
 */
function updateActiveNav(activeLink) {
    document.querySelectorAll('.nav-link').forEach(link => {
        link.classList.remove('active');
    });
    activeLink.classList.add('active');
}

// ===== SECTION NAVIGATION =====

/**
 * Show specific section
 */
function showSection(sectionName) {
    // Hide all sections
    document.querySelectorAll('.section').forEach(section => {
        section.classList.remove('active');
    });

    // Show selected section
    const targetSection = document.getElementById(`${sectionName}-section`);
    if (targetSection) {
        targetSection.classList.add('active');
        window.scrollTo(0, 0);
    }

    // Start or stop seat polling for real-time updates
    if (sectionName === 'seats') {
        startSeatStatusPolling();
    } else {
        stopSeatStatusPolling();
    }

    // Load data based on section
    if (sectionName === 'cities') {
        loadCities();
    } else if (sectionName === 'movies') {
        if (state.selectedCity) {
            loadMoviesByCity(state.selectedCity);
        } else {
            showSection('cities'); // Redirect if no city selected
        }
    } else if (sectionName === 'bookings') {
        loadUserBookings();
    }
}

/**
 * Go home
 */
function goHome() {
    unlockAllSelectedSeats();
    state.selectedSeats = [];
    clearLockTimer();
    showSection('cities');
}

// ===== CITIES SECTION =====

/**
 * Load and display all cities
 */
async function loadCities() {
    showLoading(true);
    const citiesGrid = document.getElementById('citiesGrid');
    citiesGrid.innerHTML = '<div class="loading-spinner">Loading cities...</div>';

    const cities = await fetchCities();
    showLoading(false);

    if (!cities || cities.length === 0) {
        citiesGrid.innerHTML = '<p style="color: var(--text-secondary);">No cities available</p>';
        return;
    }

    state.cities = cities;
    citiesGrid.innerHTML = '';

    // Sort so Bangalore is always first
    const sorted = [...cities].sort((a, b) => {
        if (a.toLowerCase() === 'bangalore') return -1;
        if (b.toLowerCase() === 'bangalore') return 1;
        return a.localeCompare(b);
    });

    sorted.forEach((city, index) => {
        if (index > 0) {
            const div = document.createElement('span');
            div.className = 'city-divider';
            citiesGrid.appendChild(div);
        }
        const card = document.createElement('div');
        card.className = 'city-card';
        card.innerHTML = `<span class="city-name">${city}</span>`;
        card.onclick = () => selectCity(city);
        citiesGrid.appendChild(card);
    });
}

/**
 * Create city card element
 */
function createCityCard(city) {
    const card = document.createElement('div');
    card.className = 'city-card';
    card.innerHTML = `
        <div class="city-icon">🏙️</div>
        <h3 class="city-name">${city}</h3>
        <button class="select-btn" onclick="selectCity('${city}')">Select City</button>
    `;
    return card;
}

/**
 * Select a city and navigate to movies
 */
function selectCity(city) {
    state.selectedCity = city;
    document.getElementById('selectedCity').textContent = city;
    loadMoviesByCity(city);
    showSection('movies');
}

// ===== MOVIES SECTION =====

/**
 * Load and display all movies
 */
async function loadMovies() {
    showLoading(true);
    const moviesGrid = document.getElementById('moviesGrid');
    moviesGrid.innerHTML = '<div class="loading-spinner">Loading movies...</div>';

    const movies = await fetchMovies();
    showLoading(false);

    if (!movies || movies.length === 0) {
        moviesGrid.innerHTML = '<p style="color: var(--text-secondary);">No movies available</p>';
        return;
    }

    state.movies = movies;
    moviesGrid.innerHTML = '';

    movies.forEach(movie => {
        const movieCard = createMovieCard(movie);
        moviesGrid.appendChild(movieCard);
    });
}

/**
 * Load movies by city
 */
async function loadMoviesByCity(city) {
    showLoading(true);
    const moviesGrid = document.getElementById('moviesGrid');
    moviesGrid.innerHTML = '<div class="loading-spinner">Loading movies...</div>';

    const movies = await fetchMoviesByCity(city);
    showLoading(false);

    if (!movies || movies.length === 0) {
        moviesGrid.innerHTML = '<p style="color: var(--text-secondary);">No movies available in this city</p>';
        return;
    }

    state.movies = movies;
    moviesGrid.innerHTML = '';

    movies.forEach(movie => {
        const movieCard = createMovieCard(movie);
        moviesGrid.appendChild(movieCard);
    });
}

/**
 * Create movie card element
 */
function createMovieCard(movie) {
    const card = document.createElement('div');
    card.className = 'movie-card';
    card.innerHTML = `
        <div class="movie-poster">🎬</div>
        <div class="movie-info">
            <h3 class="movie-title">${movie.name}</h3>
            <div class="movie-meta">
                <span>${movie.duration} min</span>
                <span class="movie-language">${movie.language}</span>
            </div>
            <button class="book-btn" onclick="selectMovie(${movie.id}, '${movie.name}')">
                Select Movie
            </button>
        </div>
    `;
    return card;
}

/**
 * Select a movie and load theatres in the selected city
 */
async function selectMovie(movieId, movieName) {
    state.selectedMovie = { id: movieId, name: movieName };
    state.selectedSeats = [];

    showLoading(true);
    const [theatres, shows] = await Promise.all([
        fetchTheatresByCityAndMovie(state.selectedCity, movieId),
        fetchShowsByMovieId(movieId)
    ]);
    showLoading(false);

    if (!theatres || theatres.length === 0) {
        showError('No theatres available for this movie in the selected city');
        return;
    }

    // Filter shows to only those in the selected city
    const cityTheatres = new Set(theatres.map(t => t.id));
    state.shows = shows.filter(show => cityTheatres.has(show.screen.theatre.id));

    state.theatres = theatres;
    loadTheatres();
    showSection('theatres');
}

// ===== THEATRES SECTION =====

/**
 * Load and display theatres with shows
 */
function loadTheatres() {
    const theatresContainer = document.getElementById('theatresContainer');
    theatresContainer.innerHTML = '';

    // Group shows by theatre
    const theatreMap = {};

    state.shows.forEach(show => {
        const theatreId = show.screen.theatre.id;
        if (!theatreMap[theatreId]) {
            theatreMap[theatreId] = {
                theatre: show.screen.theatre,
                shows: []
            };
        }
        theatreMap[theatreId].shows.push(show);
    });

    // Create theatre cards
    Object.values(theatreMap).forEach(theatreData => {
        const theatreCard = createTheatreCard(theatreData);
        theatresContainer.appendChild(theatreCard);
    });
}

/**
 * Create theatre card element
 */
function createTheatreCard(theatreData) {
    const { theatre, shows } = theatreData;
    
    const card = document.createElement('div');
    card.className = 'theatre-card';

    const header = document.createElement('div');
    header.className = 'theatre-header';
    header.innerHTML = `
        <div>
            <h3 class="theatre-name">${theatre.name}</h3>
            <p class="theatre-location">📍 ${theatre.location}</p>
        </div>
    `;
    card.appendChild(header);

    const showsGrid = document.createElement('div');
    showsGrid.className = 'shows-grid';

    // Filter shows by today's date
    const today = new Date();
    today.setHours(0, 0, 0, 0);
    const tomorrow = new Date(today);
    tomorrow.setDate(tomorrow.getDate() + 1);

    const todaysShows = shows.filter(show => {
        const showDate = new Date(show.showTime);
        return showDate >= today && showDate < tomorrow;
    });

    todaysShows.forEach(show => {
        const btn = document.createElement('button');
        btn.className = 'show-time-btn';
        const showTime = new Date(show.showTime).toLocaleTimeString('en-US', {
            hour: '2-digit',
            minute: '2-digit'
        });
        btn.textContent = showTime;
        btn.onclick = () => selectShow(show);
        showsGrid.appendChild(btn);
    });

    card.appendChild(showsGrid);
    return card;
}

/**
 * Select a show and load seats
 */
async function selectShow(show) {
    state.selectedShow = show;
    state.selectedSeats = [];
    state.selectedTheatre = show.screen.theatre;
    clearLockTimer();

    showLoading(true);
    const seats = await fetchSeatsByScreenAndShow(show.screen.id, show.id, currentUser.id);
    showLoading(false);

    state.screenSeats = seats || [];
    // Restore any seats already locked by this user (e.g. page refresh)
    state.selectedSeats = state.screenSeats
        .filter(s => s.status === 'LOCKED_BY_ME')
        .map(s => s.id);
    if (state.selectedSeats.length > 0) startLockTimer();
    loadSeats();
    showSection('seats');
}

// ===== SEATS SECTION =====

/**
 * Load and display seats
 */
function loadSeats() {
    const seatsContainer = document.getElementById('seatsContainer');
    seatsContainer.innerHTML = '';

    const showTime = new Date(state.selectedShow.showTime).toLocaleString();
    document.getElementById('selectedMovieTitle').textContent = state.selectedMovie.name;
    document.getElementById('selectedShowInfo').textContent =
        `${state.selectedTheatre.name} • ${showTime}`;

    state.screenSeats.forEach(seat => {
        const seatBtn = document.createElement('button');
        seatBtn.className = 'seat';
        seatBtn.textContent = seat.seatNumber;
        seatBtn.dataset.seatId = seat.id;

        if (seat.status === 'BOOKED') {
            seatBtn.classList.add('booked');
            seatBtn.disabled = true;
        } else if (seat.status === 'LOCKED') {
            seatBtn.classList.add('locked');
            seatBtn.disabled = true;
            seatBtn.title = 'Locked by another user';
        } else if (seat.status === 'LOCKED_BY_ME' || state.selectedSeats.includes(seat.id)) {
            seatBtn.classList.add('selected');
        }

        seatBtn.addEventListener('click', () => toggleSeat(seat.id, seatBtn));
        seatsContainer.appendChild(seatBtn);
    });

    updateSeatsSummary();
}

/**
 * Toggle seat selection
 */
async function toggleSeat(seatId, seatBtn) {
    if (seatBtn.classList.contains('booked') || seatBtn.classList.contains('locked')) return;

    const userId = currentUser.id;
    const showId = state.selectedShow.id;

    if (seatBtn.classList.contains('selected')) {
        // Deselect — unlock on backend and refresh seat availability
        seatBtn.classList.remove('selected');
        state.selectedSeats = state.selectedSeats.filter(id => id !== seatId);
        await unlockSeat(showId, seatId, userId);
        if (state.selectedSeats.length === 0) clearLockTimer();

        const seats = await fetchSeatsByScreenAndShow(state.selectedShow.screen.id, showId, currentUser.id);
        state.screenSeats = seats || [];
        loadSeats();
    } else {
        // Select — lock on backend
        const locked = await lockSeat(showId, seatId, userId);
        if (!locked) {
            showError('This seat was just taken. Please choose another.');
            // Refresh seat statuses
            const seats = await fetchSeatsByScreenAndShow(state.selectedShow.screen.id, showId, currentUser.id);
            state.screenSeats = seats || [];
            loadSeats();
            return;
        }
        seatBtn.classList.add('selected');
        state.selectedSeats.push(seatId);
        // Start/reset 5-min countdown on first seat selected
        if (state.selectedSeats.length === 1) startLockTimer();
    }

    updateSeatsSummary();
}

/**
 * Update seats summary
 */
function updateSeatsSummary() {
    const selectedSeatNumbers = state.selectedSeats.map(id => {
        const seat = state.screenSeats.find(s => s.id === id);
        return seat ? seat.seatNumber : id;
    });
    document.getElementById('selectedSeatsDisplay').textContent =
        selectedSeatNumbers.length > 0 ? selectedSeatNumbers.join(', ') : 'None';

    const pricePerSeat = 250;
    const totalPrice = state.selectedSeats.length * pricePerSeat;
    document.getElementById('totalPriceDisplay').textContent = `₹${totalPrice.toLocaleString()}`;

    const proceedBtn = document.getElementById('proceedBtn');
    proceedBtn.disabled = state.selectedSeats.length === 0;
}

function startSeatStatusPolling() {
    stopSeatStatusPolling();

    if (!state.selectedShow || !currentUser) return;

    state.seatRefreshInterval = setInterval(async () => {
        try {
            const seats = await fetchSeatsByScreenAndShow(state.selectedShow.screen.id, state.selectedShow.id, currentUser.id);
            if (!seats) return;
            state.screenSeats = seats;
            loadSeats();
        } catch (e) {
            // Ignore polling errors silently
        }
    }, 7000);
}

function stopSeatStatusPolling() {
    if (state.seatRefreshInterval) {
        clearInterval(state.seatRefreshInterval);
        state.seatRefreshInterval = null;
    }
}

/**
 * Proceed to booking
 */
function proceedToBooking() {
    if (state.selectedSeats.length === 0) {
        showError('Please select at least one seat');
        return;
    }

    const pricePerSeat = 250;
    const totalAmount = state.selectedSeats.length * pricePerSeat;
    const showTime = new Date(state.selectedShow.showTime).toLocaleString();
    const selectedSeatNumbers = state.selectedSeats.map(id => {
        const seat = state.screenSeats.find(s => s.id === id);
        return seat ? seat.seatNumber : id;
    });

    document.getElementById('paymentMovie').textContent = state.selectedMovie.name;
    document.getElementById('paymentTheatre').textContent = state.selectedTheatre.name;
    document.getElementById('paymentScreen').textContent = state.selectedShow.screen.name;
    document.getElementById('paymentShowTime').textContent = showTime;
    document.getElementById('paymentSeats').textContent = selectedSeatNumbers.join(', ');
    document.getElementById('paymentAmount').textContent = `₹${totalAmount.toLocaleString()}`;

    showSection('payment');
}

/**
 * Process payment
 */
async function processPayment() {
    const cardNumber = document.getElementById('cardNumber').value.trim();
    const expiryDate = document.getElementById('expiryDate').value.trim();
    const cvv = document.getElementById('cvv').value.trim();
    const cardName = document.getElementById('cardName').value.trim();

    // Simulate payment processing
    showLoading(true);
    document.getElementById('payBtn').disabled = true;
    document.getElementById('payBtn').textContent = 'Processing...';

    // Simulate network delay
    setTimeout(async () => {
        showLoading(false);
        document.getElementById('payBtn').disabled = false;
        document.getElementById('payBtn').textContent = 'Pay Now';

        // Payment done — confirm booking immediately
        await confirmBooking();
    }, 3000);
}

async function confirmBooking() {
    if (!currentUser) { showError('Please log in first'); return; }
    if (state.selectedSeats.length === 0) { showError('No seats selected'); return; }

    showLoading(true);
    const result = await createBooking(currentUser.id, state.selectedShow.id, state.selectedSeats);
    showLoading(false);

    if (result) {
        clearLockTimer();
        showSuccess(result);
    } else {
        showError('Booking failed. Please try again.');
    }
}

/**
 * Show success screen with full booking details
 */
function showSuccess(bookingData) {
    const pricePerSeat = 250;
    const totalAmount = state.selectedSeats.length * pricePerSeat;
    const showTime = new Date(state.selectedShow.showTime).toLocaleString();
    const selectedSeatNumbers = state.selectedSeats.map(id => {
        const seat = state.screenSeats.find(s => s.id === id);
        return seat ? seat.seatNumber : id;
    });

    document.getElementById('bookingIdDisplay').textContent = bookingData.bookingId;
    document.getElementById('bookingMovie').textContent = state.selectedMovie.name;
    document.getElementById('bookingTheatre').textContent = state.selectedTheatre.name;
    document.getElementById('bookingScreen').textContent = state.selectedShow.screen.name;
    document.getElementById('bookingShowTime').textContent = showTime;
    document.getElementById('bookingSeats').textContent = selectedSeatNumbers.join(', ');
    document.getElementById('bookingAmount').textContent = `₹${totalAmount.toLocaleString()}`;

    showSection('success');
    state.selectedSeats = [];
}

// ===== BOOKINGS SECTION =====

/**
 * Load and display user bookings
 */
async function loadUserBookings() {
    if (!currentUser) return;

    showLoading(true);
    const bookingsGrid = document.getElementById('bookingsGrid');
    bookingsGrid.innerHTML = '<div class="loading-spinner">Loading your bookings...</div>';

    const bookings = await fetchUserBookings(currentUser.id);
    showLoading(false);

    if (!bookings || bookings.length === 0) {
        bookingsGrid.innerHTML = '<p style="color: var(--text-secondary); grid-column: 1/-1;">No bookings found</p>';
        return;
    }

    bookingsGrid.innerHTML = '';

    bookings.forEach(booking => {
        const bookingCard = createBookingCard(booking);
        bookingsGrid.appendChild(bookingCard);
    });
}

/**
 * Create booking card element
 */
function createBookingCard(booking) {
    const card = document.createElement('div');
    card.className = 'booking-card';

    const showTime = booking.showTime ? new Date(booking.showTime).toLocaleString() : 'N/A';
    const bookedOn = booking.bookingTime ? new Date(booking.bookingTime).toLocaleDateString() : 'N/A';
    const seats = booking.seats && booking.seats.length > 0 ? booking.seats.join(', ') : 'N/A';
    const totalAmount = booking.seats ? `₹${(booking.seats.length * 250).toLocaleString()}` : 'N/A';

    card.innerHTML = `
        <span class="booking-status confirmed">CONFIRMED</span>
        <div class="booking-info">
            <h3>Booking #${booking.id}</h3>
            <p><strong>Movie:</strong> ${booking.movieName || 'N/A'}</p>
            <p><strong>Theatre:</strong> ${booking.theatreName || 'N/A'}</p>
            <p><strong>Screen:</strong> ${booking.screenName || 'N/A'}</p>
            <p><strong>Show Time:</strong> ${showTime}</p>
            <p><strong>Seats:</strong> ${seats}</p>
            <p><strong>Total Amount:</strong> ${totalAmount}</p>
            <p><strong>Booked on:</strong> ${bookedOn}</p>
        </div>
    `;

    return card;
}

// ===== SEAT LOCK TIMER =====

function startLockTimer() {
    clearLockTimer();
    state.lockExpiryTime = Date.now() + 5 * 60 * 1000; // 5 minutes
    updateTimerDisplay();
    state.lockTimerInterval = setInterval(() => {
        const remaining = state.lockExpiryTime - Date.now();
        if (remaining <= 0) {
            clearLockTimer();
            // Seats expired — reset selection and reload
            state.selectedSeats = [];
            fetchSeatsByScreenAndShow(state.selectedShow.screen.id, state.selectedShow.id)
                .then(seats => { state.screenSeats = seats || []; loadSeats(); });
            showError('Your seat reservation has expired. Please select seats again.');
        } else {
            updateTimerDisplay();
        }
    }, 1000);
}

function clearLockTimer() {
    if (state.lockTimerInterval) {
        clearInterval(state.lockTimerInterval);
        state.lockTimerInterval = null;
    }
    state.lockExpiryTime = null;
    const el = document.getElementById('lockTimer');
    if (el) el.textContent = '';
}

function updateTimerDisplay() {
    const el = document.getElementById('lockTimer');
    if (!el || !state.lockExpiryTime) return;
    const remaining = Math.max(0, state.lockExpiryTime - Date.now());
    const mins = Math.floor(remaining / 60000);
    const secs = Math.floor((remaining % 60000) / 1000);
    el.textContent = `⏳ Seats locked for ${mins}:${secs.toString().padStart(2, '0')}`;
    el.style.color = remaining < 60000 ? '#ff4444' : '#00ffcc';
}

async function unlockAllSelectedSeats() {
    if (state.selectedSeats.length === 0 || !state.selectedShow || !currentUser) return;
    const showId = state.selectedShow.id;
    await Promise.all(state.selectedSeats.map(seatId => unlockSeat(showId, seatId, currentUser.id)));
}

// ===== UI UTILITIES =====

/**
 * Show loading overlay
 */
function showLoading(show) {
    const overlay = document.getElementById('loadingOverlay');
    if (show) {
        overlay.classList.add('active');
    } else {
        overlay.classList.remove('active');
    }
}

/**
 * Show error modal
 */
function showError(message) {
    document.getElementById('errorMessage').textContent = message;
    const modal = document.getElementById('errorModal');
    modal.classList.add('active');
}

/**
 * Close modal
 */
function closeModal() {
    const modal = document.getElementById('errorModal');
    modal.classList.remove('active');
}

/**
 * Close modal on background click
 */
window.addEventListener('click', (event) => {
    const modal = document.getElementById('errorModal');
    if (event.target === modal) {
        closeModal();
    }
});

// ===== KEYBOARD SHORTCUTS =====
document.addEventListener('keydown', (e) => {
    if (e.key === 'Escape') {
        closeModal();
    }
});

// ===== PAGE VISIBILITY =====
document.addEventListener('visibilitychange', () => {
    if (!document.hidden && window.location.href.includes('bookings')) {
        loadUserBookings();
    }
});

// ===== CAROUSEL ===== 
/**
 * Initialize movie poster carousel with auto-scrolling
 */
function initializeCarousel() {
    loadCarouselImages();
    setupCarouselInfiniteScroll();
}

/**
 * Load movie poster images from /images/ directory
 */
function loadCarouselImages() {
    const carouselTrack = document.getElementById('carouselTrack');
    if (!carouselTrack) return;

    // Movie poster images available in the project
    const posterImages = [
        'Avengers.jpg',
        'barbie.jpg',
        'Dune.jpeg',
        'Kantara.jpg',
        'Oppenheimer.jpg',
        'RRR.jpg'
    ];

    // Clear existing items
    carouselTrack.innerHTML = '';

    // Create carousel items for initial load
    posterImages.forEach(image => {
        const item = createCarouselItem(image);
        carouselTrack.appendChild(item);
    });

    // Duplicate items for seamless infinite scroll
    posterImages.forEach(image => {
        const item = createCarouselItem(image);
        carouselTrack.appendChild(item);
    });

    carouselTrack.classList.add('duplicated');
}

/**
 * Create a carousel item with movie poster image
 */
function createCarouselItem(imagePath) {
    const item = document.createElement('div');
    item.className = 'carousel-item';
    item.innerHTML = `
        <img src="/images/${imagePath}" alt="Movie Poster" class="carousel-image">
        <div class="carousel-poster-overlay"></div>
    `;
    return item;
}

/**
 * Setup infinite scroll for carousel (restart animation when complete)
 */
function setupCarouselInfiniteScroll() {
    const carouselTrack = document.getElementById('carouselTrack');
    if (!carouselTrack) return;

    // The animation is handled purely by CSS @keyframes scroll-left
    // The duplicated content ensures a seamless loop
    
    // Optional: Reset animation after completion for perfect loop
    carouselTrack.addEventListener('animationiteration', () => {
        // Animation will restart automatically with linear infinite
    });
}
