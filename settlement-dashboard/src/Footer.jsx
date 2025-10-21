export default function Footer() {
    return (
        <footer className="footer">
            <div className="footer-container">
                <div className="footer-left">
                    <h3>Re:Life</h3>
                    <p>Zero Waste Shop<br />Make a Difference with Every Gift</p>
                    <div className="footer-icons">
                        <span>●</span><span>●</span><span>●</span>
                    </div>
                </div>

                <div className="footer-links">
                    <div>
                        <h4>Socials</h4>
                        <a href="#">Discord</a>
                        <a href="#">Instagram</a>
                        <a href="#">Twitter</a>
                        <a href="#">Facebook</a>
                    </div>
                    <div>
                        <h4>Member</h4>
                        <a href="https://github.com/hyejin-jun">Jeon Hyejin</a>
                        <a href="https://github.com/leehwoo2290">Lee Hyunwoo</a>
                        <a href="https://github.com/junyoung326">Lee Junyoung</a>
                        <a href="https://github.com/hymmmmi">Bae HyeMin</a>
                    </div>
                    <div>
                        <h4>Member</h4>
                        <a href="https://github.com/DoTiKwak">Kwak Hyunchang</a>
                        <a href="https://github.com/dlgksmf0304">Lee Haneul</a>
                        <a href="https://github.com/jeongin03">Kwon Jeongin</a>
                    </div>
                </div>
            </div>

            <div className="footer-bottom">
                <p>©2025 MBC Company Name. All rights reserved</p>
                <div>
                    <a style={{ color: 'white' }}>Privacy & Policy</a> | <a style={{ color: 'white' }}>Terms & Condition</a>
                </div>
            </div>
        </footer>
    );
}