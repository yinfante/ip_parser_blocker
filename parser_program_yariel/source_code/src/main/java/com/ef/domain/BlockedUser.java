package com.ef.domain;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.Date;

/**
 * Entity representation of BLOCKED_USER table. This table has all ip users that have been blocked.
 *
 * @author yinfante
 */
@Entity
@Data
public class BlockedUser {

    @Id
    private long id;
    private int requests;
    private Date blockedDate;
    private String ip;
    private String comment;
}
