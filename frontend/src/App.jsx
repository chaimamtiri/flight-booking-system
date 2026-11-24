
import { useCallback, useEffect, useState } from 'react'
import './App.css'

const passengers = [
  { id: 'P001', name: 'Sarah Martin' },
  { id: 'P002', name: 'Adam Ali' },
  { id: 'P003', name: 'Lina Ben Ali' }
]

function App() {
  const [flights, setFlights] = useState([])
  const [bookings, setBookings] = useState([])

  const [origin, setOrigin] = useState('')
  const [destination, setDestination] = useState('')
  const [passengerId, setPassengerId] = useState('P003')

  const [selectedFlight, setSelectedFlight] = useState(null)
  const [confirmation, setConfirmation] = useState(null)

  const [loading, setLoading] = useState(true)
  const [bookingsLoading, setBookingsLoading] = useState(true)
  const [bookingLoading, setBookingLoading] = useState(false)
  const [cancellingId, setCancellingId] = useState(null)

  const [error, setError] = useState('')
  const [bookingsError, setBookingsError] = useState('')
  const [bookingError, setBookingError] = useState('')
  const [notice, setNotice] = useState('')

  const loadFlights = useCallback(async () => {
    try {
      const response = await fetch('/api/flights')

      if (!response.ok) {
        throw new Error('Unable to load flights')
      }

      setFlights(await response.json())
      setError('')
    } catch (err) {
      setError(err.message)
    } finally {
      setLoading(false)
    }
  }, [])

  const loadBookings = useCallback(async (id) => {
    setBookingsLoading(true)

    try {
      const response = await fetch(
        `/api/bookings?passengerId=${encodeURIComponent(id)}`
      )

      if (!response.ok) {
        throw new Error('Unable to load bookings')
      }

      setBookings(await response.json())
      setBookingsError('')
    } catch (err) {
      setBookings([])
      setBookingsError(err.message)
    } finally {
      setBookingsLoading(false)
    }
  }, [])

  useEffect(() => {
    loadFlights()
  }, [loadFlights])

  useEffect(() => {
    loadBookings(passengerId)
  }, [passengerId, loadBookings])

  const filteredFlights = flights.filter(flight =>
    flight.origin.toLowerCase()
      .includes(origin.trim().toLowerCase()) &&
    flight.destination.toLowerCase()
      .includes(destination.trim().toLowerCase())
  )

  function openBooking(flight) {
    setSelectedFlight(flight)
    setBookingError('')
    setConfirmation(null)
    setNotice('')
  }

  async function handleBooking(event) {
    event.preventDefault()

    if (!selectedFlight || bookingLoading) return

    setBookingLoading(true)
    setBookingError('')

    const bookingId = 'B' +
      crypto.randomUUID()
        .replaceAll('-', '')
        .slice(0, 16)
        .toUpperCase()

    try {
      const response = await fetch('/api/bookings', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({
          bookingId,
          passengerId,
          flightNumber: selectedFlight.flightNumber
        })
      })

      const data = await response.json().catch(() => ({}))

      if (!response.ok) {
        throw new Error(
          data.message || 'Booking could not be completed'
        )
      }

      setSelectedFlight(null)
      setConfirmation(data)
      setNotice('')

      await Promise.all([
        loadFlights(),
        loadBookings(passengerId)
      ])

    } catch (err) {
      setBookingError(err.message)
    } finally {
      setBookingLoading(false)
    }
  }

  async function handleCancel(bookingId) {
    const confirmed = window.confirm(
      `Cancel booking ${bookingId}?`
    )

    if (!confirmed || cancellingId !== null) return

    setCancellingId(bookingId)
    setBookingsError('')
    setNotice('')

    try {
      const response = await fetch(
        `/api/bookings/${encodeURIComponent(bookingId)}/cancel`,
        { method: 'PATCH' }
      )

      if (!response.ok) {
        const data = await response.json().catch(() => ({}))

        throw new Error(
          data.message || 'Unable to cancel booking'
        )
      }

      // HTTP 204 returns no response body
      setNotice(`Booking ${bookingId} cancelled successfully`)
      setConfirmation(null)

      await Promise.all([
        loadFlights(),
        loadBookings(passengerId)
      ])

    } catch (err) {
      setBookingsError(err.message)
    } finally {
      setCancellingId(null)
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

        <section className="passenger-selector">
          <label htmlFor="passenger">
            Demo passenger
          </label>

          <select
            id="passenger"
            value={passengerId}
            disabled={bookingLoading || cancellingId !== null}
            onChange={e => {
              setPassengerId(e.target.value)
              setSelectedFlight(null)
              setConfirmation(null)
              setNotice('')
            }}
          >
            {passengers.map(passenger => (
              <option key={passenger.id} value={passenger.id}>
                {passenger.id} — {passenger.name}
              </option>
            ))}
          </select>
        </section>

        {confirmation && (
          <div className="confirmation" role="status">
            <h3>✓ Booking confirmed!</h3>
            <p>Booking ID: {confirmation.bookingId}</p>
            <p>Flight: {confirmation.flightNumber}</p>
            <p>Status: {confirmation.status}</p>
          </div>
        )}

        {notice && (
          <div className="confirmation" role="status">
            {notice}
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
          <p className="error">{error}</p>
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

        <section className="bookings-section">
          <div className="results-header">
            <h2>My Bookings (Demo)</h2>
            <span>{passengerId}</span>
          </div>

          {bookingsLoading && (
            <p>Loading bookings...</p>
          )}

          {bookingsError && (
            <p className="error" role="alert">
              {bookingsError}
            </p>
          )}

          {!bookingsLoading &&
            !bookingsError &&
            bookings.length === 0 && (
              <p>No bookings for this passenger.</p>
            )}

          <div className="bookings-list">
            {!bookingsLoading &&
              bookings.map(booking => (
                <article
                  className="booking-card"
                  key={booking.bookingId}
                >
                  <div className="booking-details">
                    <strong>{booking.bookingId}</strong>
                    <p>Passenger: {booking.passengerId}</p>
                    <p>Flight: {booking.flightNumber}</p>
                  </div>

                  <div className="booking-actions">
                    <span className={
                      booking.status === 'CONFIRMED'
                        ? 'status-confirmed'
                        : 'status-cancelled'
                    }>
                      {booking.status}
                    </span>

                    {booking.status === 'CONFIRMED' && (
                      <button
                        className="cancel-booking-button"
                        disabled={cancellingId !== null}
                        onClick={() =>
                          handleCancel(booking.bookingId)
                        }
                      >
                        {cancellingId === booking.bookingId
                          ? 'Cancelling...'
                          : 'Cancel booking'}
                      </button>
                    )}
                  </div>
                </article>
              ))
            }
          </div>
        </section>

        {selectedFlight && (
          <div className="modal-overlay">
            <div
              className="booking-modal"
              role="dialog"
              aria-modal="true"
              aria-labelledby="booking-title"
            >
              <h2 id="booking-title">
                Book your flight
              </h2>

              <p>
                Flight: {selectedFlight.flightNumber}
              </p>

              <p>
                {selectedFlight.origin}
                {' → '}
                {selectedFlight.destination}
              </p>

              <p>
                Price: {Number(selectedFlight.price).toFixed(2)} €
              </p>

              <p>Passenger: {passengerId}</p>

              <form onSubmit={handleBooking}>
                {bookingError && (
                  <p className="booking-error" role="alert">
                    {bookingError}
                  </p>
                )}

                <div className="modal-actions">
                  <button
                    type="button"
                    className="cancel-button"
                    disabled={bookingLoading}
                    onClick={() => setSelectedFlight(null)}
                  >
                    Close
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
