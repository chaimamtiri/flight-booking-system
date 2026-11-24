import { useEffect, useState } from 'react'
import './App.css'

function App() {
  const [flights, setFlights] = useState([])
  const [origin, setOrigin] = useState('')
  const [destination, setDestination] = useState('')
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  useEffect(() => {
    async function loadFlights() {
      try {
        const response = await fetch('/api/flights')

        if (!response.ok) {
          throw new Error('Unable to load flights')
        }

        setFlights(await response.json())
      } catch (err) {
        setError(err.message)
      } finally {
        setLoading(false)
      }
    }

    loadFlights()
  }, [])

  const filteredFlights = flights.filter(flight =>
    flight.origin.toLowerCase().includes(origin.toLowerCase()) &&
    flight.destination.toLowerCase().includes(destination.toLowerCase())
  )

  return (
    <div className="app">
      <header>
        <h2>✈ Flight Booking System</h2>
        <span>Explore your next destination</span>
      </header>

      <section className="hero">
        <h1>Find your next flight</h1>
        <p>Discover available flights and explore destinations.</p>
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

        <div className="results-header">
          <h2>Available Flights</h2>
          {!loading && !error && (
            <span>{filteredFlights.length} flights found</span>
          )}
        </div>

        {loading && <p>Loading flights...</p>}
        {error && <p className="error">{error}</p>}

        {!loading && !error && filteredFlights.length === 0 && (
          <p>No flights found for this search.</p>
        )}

        <section className="flight-list">
          {!loading && !error && filteredFlights.map(flight => (
            <article className="flight-card" key={flight.flightNumber}>
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
                <strong>{Number(flight.price).toFixed(2)} €</strong>
                <button disabled>Book flight — coming soon</button>
              </div>
            </article>
          ))}
        </section>
      </main>
    </div>
  )
}

export default App
