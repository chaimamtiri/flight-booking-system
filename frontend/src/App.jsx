
import { useEffect, useState } from 'react'
import './App.css'

function App() {
  const [flights, setFlights] = useState([])
  const [origin, setOrigin] = useState('')
  const [destination, setDestination] = useState('')
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  const [selectedFlight, setSelectedFlight] = useState(null)
  const [passengerId, setPassengerId] = useState('P003')
  const [bookingLoading, setBookingLoading] = useState(false)
  const [bookingError, setBookingError] = useState('')
  const [confirmation, setConfirmation] = useState(null)

  async function loadFlights() {
    try {
      const response = await fetch('/api/flights')

      if (!response.ok) {
        throw new Error('Unable to load flights')
      }

      const data = await response.json()
      setFlights(data)
      setError('')
    } catch (err) {
      setError(err.message)
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    loadFlights()
  }, [])

  const filteredFlights = flights.filter(flight =>
    flight.origin.toLowerCase()
      .includes(origin.trim().toLowerCase()) &&
    flight.destination.toLowerCase()
      .includes(destination.trim().toLowerCase())
  )

  function openBooking(flight) {
    setSelectedFlight(flight)
    setPassengerId('P003')
    setBookingError('')
    setConfirmation(null)
  }

  function closeBooking() {
    if (bookingLoading) return

    setSelectedFlight(null)
    setBookingError('')
  }

  async function handleBooking(event) {
    event.preventDefault()

    if (!selectedFlight || bookingLoading) return

    setBookingLoading(true)
    setBookingError('')

    // PostgreSQL booking_id is limited to 20 characters
    const bookingId =
      'B' + crypto.randomUUID()
        .replaceAll('-', '')
        .slice(0, 16)
        .toUpperCase()

    const requestBody = {
      bookingId,
      passengerId,
      flightNumber: selectedFlight.flightNumber
    }

    try {
      const response = await fetch('/api/bookings', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify(requestBody)
      })

      const data = await response.json()

      if (!response.ok) {
        throw new Error(
          data.message || 'Booking could not be completed'
        )
      }

      setConfirmation(data)
      setSelectedFlight(null)

      // Refresh available seats after a successful booking
      await loadFlights()

    } catch (err) {
      setBookingError(err.message)
    } finally {
      setBookingLoading(false)
    }
  }

  return (
    <div className="app">

      <header>
        <h2>✈ Flight Booking System</h2>
        <span>Explore your next destination</span>
      </header>

      <section className="hero">
        <h1>Find your next flight</h1>
        <p>
          Discover available flights and explore destinations.
        </p>
      </section>

      <main>
        <section className="search">
          <div>
            <label htmlFor="origin">From</label>
            <input
              id="origin"
              placeholder="Departure city"
              value={origin}
              onChange={e => setOrigin(e.target.value)}
            />
          </div>

          <div>
            <label htmlFor="destination">To</label>
            <input
              id="destination"
              placeholder="Arrival city"
              value={destination}
              onChange={e => setDestination(e.target.value)}
            />
          </div>

          <button onClick={() => {
            setOrigin('')
            setDestination('')
          }}>
            Clear filters
          </button>
        </section>

        {confirmation && (
          <div className="confirmation">
            <h3>✓ Booking confirmed!</h3>
            <p>
              Booking ID: <strong>{confirmation.bookingId}</strong>
            </p>
            <p>
              Passenger: {confirmation.passengerId}
            </p>
            <p>
              Flight: {confirmation.flightNumber}
            </p>
            <p>
              Status: <strong>{confirmation.status}</strong>
            </p>
            <button onClick={() => setConfirmation(null)}>
              Close confirmation
            </button>
          </div>
        )}

        <div className="results-header">
          <h2>Available Flights</h2>
          {!loading && !error && (
            <span>{filteredFlights.length} flights found</span>
          )}
        </div>

        {loading && <p>Loading flights...</p>}

        {error && (
          <p className="error">
            {error}. Check that Spring Boot is running.
          </p>
        )}

        {!loading && !error &&
          filteredFlights.length === 0 && (
            <p>No flights found for this search.</p>
          )}

        <section className="flight-list">
          {!loading && !error &&
            filteredFlights.map(flight => (
              <article
                className="flight-card"
                key={flight.flightNumber}
              >
                <div className="flight-info">
                  <span className="flight-number">
                    {flight.flightNumber}
                  </span>

                  <div className="route">
                    <strong>{flight.origin}</strong>
                    <span>✈</span>
                    <strong>{flight.destination}</strong>
                  </div>

                  <p>
                    {flight.availableSeats} seats available
                  </p>
                </div>

                <div className="price">
                  <small>Price per seat</small>
                  <strong>
                    {Number(flight.price).toFixed(2)} €
                  </strong>

                  <button
                    disabled={flight.availableSeats === 0}
                    onClick={() => openBooking(flight)}
                  >
                    {flight.availableSeats === 0
                      ? 'Flight full'
                      : 'Book flight'}
                  </button>
                </div>
              </article>
            ))
          }
        </section>

        {selectedFlight && (
          <div className="modal-overlay">
            <div
              className="booking-modal"
              role="dialog"
              aria-modal="true"
              aria-labelledby="booking-title"
            >
              <h2 id="booking-title">Book your flight</h2>

              <p>
                Flight: <strong>
                  {selectedFlight.flightNumber}
                </strong>
              </p>

              <p>
                {selectedFlight.origin}
                {' → '}
                {selectedFlight.destination}
              </p>

              <p>
                Price: <strong>
                  {Number(selectedFlight.price).toFixed(2)} €
                </strong>
              </p>

              <form onSubmit={handleBooking}>
                <label htmlFor="passengerId">
                  Select passenger
                </label>

                <select
                  id="passengerId"
                  value={passengerId}
                  onChange={e =>
                    setPassengerId(e.target.value)
                  }
                  disabled={bookingLoading}
                >
                  <option value="P001">
                    P001 — Sarah Martin
                  </option>
                  <option value="P002">
                    P002 — Adam Ali
                  </option>
                  <option value="P003">
                    P003 — Lina Ben Ali
                  </option>
                </select>

                {bookingError && (
                  <p className="booking-error" role="alert">
                    {bookingError}
                  </p>
                )}

                <div className="modal-actions">
                  <button
                    type="button"
                    className="cancel-button"
                    onClick={closeBooking}
                    disabled={bookingLoading}
                  >
                    Cancel
                  </button>

                  <button
                    type="submit"
                    disabled={bookingLoading}
                  >
                    {bookingLoading
                      ? 'Booking...'
                      : 'Confirm booking'}
                  </button>
                </div>
              </form>
            </div>
          </div>
        )}

      </main>
    </div>
  )
}

export default App
