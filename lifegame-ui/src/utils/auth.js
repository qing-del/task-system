export const clearAuthToken = () => {
    localStorage.removeItem('token');

    console.log("Auth token cleared.");
}