# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added

### Changed

### Fixed

### Removed

## [1.2.0] - 2022-05-13

### Added

- Property `throwExceptionOnFailure` was added in `SeniorXHTTPRouteBuilder` builder and `AuthenticationAPI` constructor. Default value: `true`.
  *  By default, Camel throws `HttpOperationFailedException` for failed response codes. If set to `false`, allows you to get any response from the remote server.

### Changed

- Attribute `path` in `PrimitiveType` set to public.

## [1.1.0] - 2022-04-04

### Added

- Entity primitive type.

## [1.0.0] - 2022-03-11

### Added

- Integration consumer from Senior X events.
- Integration for Senior X APIs.
