import { useState } from 'react';
import Login from './pages/Login';
import Register from './pages/Register';
import ForgotPassword from './pages/ForgotPassword';
import { BrowserRouter ,Routes,Route,Navigate} from 'react-router-dom';


function App() {
  const [count, setCount] = useState(0)

  return (
    <BrowserRouter>
          <Routes>
            <Route path="/" element={<Navigate to="/login" />} />
            <Route path="/login" element={<Login/>} />
             <Route path="/register" element={<Register/>}/>
              <Route path="forgotPassword" element={<ForgotPassword/>}/>
          </Routes>

    </BrowserRouter>
  )
}

export default App
