package ru.alamics.sso.jpa.entity;

import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;
import ru.alamics.sso.jpa.entity.common.ImportUsersDataStatus;

import jakarta.persistence.*;

@NoArgsConstructor
@Entity
@Table(name = "IMPORT_USERS_DATA")
public class ImportUsersDataEntity {
    @Id
    @GeneratedValue(generator = "system-uuid")
    @GenericGenerator(name = "system-uuid", strategy = "uuid")
    private String id;

    //@ManyToOne(targetEntity = ImportUsersReportEntity.class, fetch = FetchType.LAZY)
    @Column(name = "import_id")
    private String importUsersReport;

    @Column(name = "first_name")
    private String firstName;
    @Column(name = "email")
    private String email;
    @Column(name = "phone")
    private String phone;
    @Column(name = "toms_id")
    private String tomsId;
    @Column(name = "dmp_id")
    private String dmpId;
    @Column(name = "MARK_BRAND_ID")
    private String markBrandId;
    @Column(name = "personal_account")
    private String personalAccount;
    @Column(name = "personal_account_user")
    private String personalAccountUser;
    @Column(name = "role")
    private String role;
    @Column(name = "systems")
    private String systems;
    @Column(name = "is_created")
    private boolean isCreated;
    @Column(name = "user_id")
    private String userId;
    @Column(name = "errors")
    private String errors;
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private ImportUsersDataStatus status = ImportUsersDataStatus.AWAITING;
    @Column
    private String cleanPassword;

    public String getId() {
        return this.id;
    }

    public String getImportUsersReport() {
        return this.importUsersReport;
    }

    public String getFirstName() {
        return this.firstName;
    }

    public String getEmail() {
        return this.email;
    }

    public String getPhone() {
        return this.phone;
    }

    public String getTomsId() {
        return this.tomsId;
    }

    public String getDmpId() {
        return this.dmpId;
    }

    public String getMarkBrandId() {
        return this.markBrandId;
    }

    public String getPersonalAccount() {
        return this.personalAccount;
    }

    public String getPersonalAccountUser() {
        return this.personalAccountUser;
    }

    public String getRole() {
        return this.role;
    }

    public String getSystems() {
        return this.systems;
    }

    public boolean isCreated() {
        return this.isCreated;
    }

    public String getUserId() {
        return this.userId;
    }

    public String getErrors() {
        return this.errors;
    }

    public ImportUsersDataStatus getStatus() {
        return this.status;
    }

    public String getCleanPassword() {
        return this.cleanPassword;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setImportUsersReport(String importUsersReport) {
        this.importUsersReport = importUsersReport;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setTomsId(String tomsId) {
        this.tomsId = tomsId;
    }

    public void setDmpId(String dmpId) {
        this.dmpId = dmpId;
    }

    public void setMarkBrandId(String markBrandId) {
        this.markBrandId = markBrandId;
    }

    public void setPersonalAccount(String personalAccount) {
        this.personalAccount = personalAccount;
    }

    public void setPersonalAccountUser(String personalAccountUser) {
        this.personalAccountUser = personalAccountUser;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public void setSystems(String systems) {
        this.systems = systems;
    }

    public void setCreated(boolean isCreated) {
        this.isCreated = isCreated;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setErrors(String errors) {
        this.errors = errors;
    }

    public void setStatus(ImportUsersDataStatus status) {
        this.status = status;
    }

    public void setCleanPassword(String cleanPassword) {
        this.cleanPassword = cleanPassword;
    }

    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof ImportUsersDataEntity)) return false;
        final ImportUsersDataEntity other = (ImportUsersDataEntity) o;
        if (!other.canEqual(this)) return false;
        final Object this$id = this.getId();
        final Object other$id = other.getId();
        if (this$id == null ? other$id != null : !this$id.equals(other$id)) return false;
        final Object this$importUsersReport = this.getImportUsersReport();
        final Object other$importUsersReport = other.getImportUsersReport();
        if (this$importUsersReport == null ? other$importUsersReport != null : !this$importUsersReport.equals(other$importUsersReport))
            return false;
        final Object this$firstName = this.getFirstName();
        final Object other$firstName = other.getFirstName();
        if (this$firstName == null ? other$firstName != null : !this$firstName.equals(other$firstName)) return false;
        final Object this$email = this.getEmail();
        final Object other$email = other.getEmail();
        if (this$email == null ? other$email != null : !this$email.equals(other$email)) return false;
        final Object this$phone = this.getPhone();
        final Object other$phone = other.getPhone();
        if (this$phone == null ? other$phone != null : !this$phone.equals(other$phone)) return false;
        final Object this$tomsId = this.getTomsId();
        final Object other$tomsId = other.getTomsId();
        if (this$tomsId == null ? other$tomsId != null : !this$tomsId.equals(other$tomsId)) return false;
        final Object this$dmpId = this.getDmpId();
        final Object other$dmpId = other.getDmpId();
        if (this$dmpId == null ? other$dmpId != null : !this$dmpId.equals(other$dmpId)) return false;
        final Object this$markBrandId = this.getMarkBrandId();
        final Object other$markBrandId = other.getMarkBrandId();
        if (this$markBrandId == null ? other$markBrandId != null : !this$markBrandId.equals(other$markBrandId))
            return false;
        final Object this$personalAccount = this.getPersonalAccount();
        final Object other$personalAccount = other.getPersonalAccount();
        if (this$personalAccount == null ? other$personalAccount != null : !this$personalAccount.equals(other$personalAccount))
            return false;
        final Object this$personalAccountUser = this.getPersonalAccountUser();
        final Object other$personalAccountUser = other.getPersonalAccountUser();
        if (this$personalAccountUser == null ? other$personalAccountUser != null : !this$personalAccountUser.equals(other$personalAccountUser))
            return false;
        final Object this$role = this.getRole();
        final Object other$role = other.getRole();
        if (this$role == null ? other$role != null : !this$role.equals(other$role)) return false;
        final Object this$systems = this.getSystems();
        final Object other$systems = other.getSystems();
        if (this$systems == null ? other$systems != null : !this$systems.equals(other$systems)) return false;
        if (this.isCreated() != other.isCreated()) return false;
        final Object this$userId = this.getUserId();
        final Object other$userId = other.getUserId();
        if (this$userId == null ? other$userId != null : !this$userId.equals(other$userId)) return false;
        final Object this$errors = this.getErrors();
        final Object other$errors = other.getErrors();
        if (this$errors == null ? other$errors != null : !this$errors.equals(other$errors)) return false;
        final Object this$status = this.getStatus();
        final Object other$status = other.getStatus();
        if (this$status == null ? other$status != null : !this$status.equals(other$status)) return false;
        final Object this$cleanPassword = this.getCleanPassword();
        final Object other$cleanPassword = other.getCleanPassword();
        if (this$cleanPassword == null ? other$cleanPassword != null : !this$cleanPassword.equals(other$cleanPassword))
            return false;
        return true;
    }

    protected boolean canEqual(final Object other) {
        return other instanceof ImportUsersDataEntity;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $id = this.getId();
        result = result * PRIME + ($id == null ? 43 : $id.hashCode());
        final Object $importUsersReport = this.getImportUsersReport();
        result = result * PRIME + ($importUsersReport == null ? 43 : $importUsersReport.hashCode());
        final Object $firstName = this.getFirstName();
        result = result * PRIME + ($firstName == null ? 43 : $firstName.hashCode());
        final Object $email = this.getEmail();
        result = result * PRIME + ($email == null ? 43 : $email.hashCode());
        final Object $phone = this.getPhone();
        result = result * PRIME + ($phone == null ? 43 : $phone.hashCode());
        final Object $tomsId = this.getTomsId();
        result = result * PRIME + ($tomsId == null ? 43 : $tomsId.hashCode());
        final Object $dmpId = this.getDmpId();
        result = result * PRIME + ($dmpId == null ? 43 : $dmpId.hashCode());
        final Object $markBrandId = this.getMarkBrandId();
        result = result * PRIME + ($markBrandId == null ? 43 : $markBrandId.hashCode());
        final Object $personalAccount = this.getPersonalAccount();
        result = result * PRIME + ($personalAccount == null ? 43 : $personalAccount.hashCode());
        final Object $personalAccountUser = this.getPersonalAccountUser();
        result = result * PRIME + ($personalAccountUser == null ? 43 : $personalAccountUser.hashCode());
        final Object $role = this.getRole();
        result = result * PRIME + ($role == null ? 43 : $role.hashCode());
        final Object $systems = this.getSystems();
        result = result * PRIME + ($systems == null ? 43 : $systems.hashCode());
        result = result * PRIME + (this.isCreated() ? 79 : 97);
        final Object $userId = this.getUserId();
        result = result * PRIME + ($userId == null ? 43 : $userId.hashCode());
        final Object $errors = this.getErrors();
        result = result * PRIME + ($errors == null ? 43 : $errors.hashCode());
        final Object $status = this.getStatus();
        result = result * PRIME + ($status == null ? 43 : $status.hashCode());
        final Object $cleanPassword = this.getCleanPassword();
        result = result * PRIME + ($cleanPassword == null ? 43 : $cleanPassword.hashCode());
        return result;
    }

    public String toString() {
        return "ImportUsersDataEntity(id=" + this.getId() + ", importUsersReport=" + this.getImportUsersReport() + ", firstName=" + this.getFirstName() + ", email=" + this.getEmail() + ", phone=" + this.getPhone() + ", tomsId=" + this.getTomsId() + ", dmpId=" + this.getDmpId() + ", markBrandId=" + this.getMarkBrandId() + ", personalAccount=" + this.getPersonalAccount() + ", personalAccountUser=" + this.getPersonalAccountUser() + ", role=" + this.getRole() + ", systems=" + this.getSystems() + ", isCreated=" + this.isCreated() + ", userId=" + this.getUserId() + ", errors=" + this.getErrors() + ", status=" + this.getStatus() + ", cleanPassword=" + this.getCleanPassword() + ")";
    }
}
