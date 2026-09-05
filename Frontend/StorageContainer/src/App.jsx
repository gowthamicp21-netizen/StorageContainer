import { useState } from 'react';
import Login from './pages/Login';
import Register from './pages/Register';
import ForgotPassword from './pages/ForgotPassword';
import { BrowserRouter ,Routes,Route,Navigate} from 'react-router-dom';
import OAuth2Success from "./pages/OAuth2Success";
import DashBoard from './pages/DashBoard';
import ProtectedRoute from "./pages/ProtectedRoute";
import SharedWithMe from "./pages/SharedWithMe";
import SharedFolder from "./pages/SharedFolder";
import SharedFileViewer from "./pages/SharedFileViewer";
function App() {
  const [count, setCount] = useState(0)

  return (
    <BrowserRouter>
          <Routes>
                  <Route path="/" element={<Navigate to="/login" />} />
                  <Route path="/login" element={<Login/>} />
                  <Route path="/register" element={<Register/>}/>
                  
                    <Route path="/forgotPassword" element={<ForgotPassword/>}/>
                    <Route path="/oauth2/success" element={<OAuth2Success />}/>

                    <Route element={<ProtectedRoute/>}>

                    <Route path='/dashboard' element={<DashBoard/>}/>
                    <Route path="/shared-with-me" element={<SharedWithMe/>}/>
                    <Route path="/shared-folder/:folderId" element={<SharedFolder />}/>
                    <Route path="/shared-file/:fileId" element={<SharedFileViewer />}/>
                    </Route>              
          </Routes>

    </BrowserRouter>
  )
}

export default App
