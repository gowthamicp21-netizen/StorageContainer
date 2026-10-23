import Login from './pages/Login';
import Register from './pages/Register';
import ForgotPassword from './pages/ForgotPassword';
import ResetPassword from './pages/ResetPassword';
import { BrowserRouter ,Routes,Route,Navigate} from 'react-router-dom';
import OAuth2Success from "./pages/OAuth2Success";
import DashBoard from './pages/DashBoard';
import ProtectedRoute from "./pages/ProtectedRoute";
import SharedWithMe from "./pages/SharedWithMe";
import SharedFolder from "./pages/SharedFolder";
import SharedFileViewer from "./pages/SharedFileViewer";
import PublicShare from "./pages/PublicShare";
function App() {

  return (
    <BrowserRouter>
          <Routes>
                  <Route path="/" element={<Navigate to="/login" />} />
                  <Route path="/login" element={<Login/>} />
                  <Route path="/register" element={<Register/>}/>
                  
                    <Route path="/forgotPassword" element={<ForgotPassword/>}/>
                    <Route path="/reset-password" element={<ResetPassword/>}/>
                    <Route path="/oauth2/success" element={<OAuth2Success />}/>

                    {/* Outside ProtectedRoute on purpose: the point of a share link is
                        that the recipient does not have an account or a token. */}
                    <Route path="/s/:token" element={<PublicShare />}/>

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
