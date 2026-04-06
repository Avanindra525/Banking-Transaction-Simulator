# Banking Simulator Full Implementation TODO

## Milestone 1: Environment & Models
- [x] Provided DB create commands
- [x] Create User model & UserRepository
- [x] Migrate AuthService to JPA UserService (fixed compiles)
- [x] Fixed AuthController
- [x] Add user_id @ManyToOne to Account model + getters/setters
- [x] Update AccountRepository with user queries
- [ ] Update AccountService for user-aware CRUD
- [ ] Frontend account ops pass user ID (session)
- [ ] Update application.properties


## Milestone 2: Account Management
- [ ] Add user_id to Account, filter by logged user

## Milestone 3: Transactions & Reports
- [ ] Transaction model/service/UI (deposit/withdraw/transfer)
- [ ] Report service (history/PDF)
- [ ] Email notifications

## Milestone 4: Alerts & Full System
- [ ] Alert model/service (low balance cron)
- [ ] Dashboard & all UI pages
- [ ] Security (session/JWT)
- [ ] Full tests/scenarios

**Next Step: Confirm this plan, then execute Step 1: Create User.java & UserRepository.java (read more files if needed)?**
